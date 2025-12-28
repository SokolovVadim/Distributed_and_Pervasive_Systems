package it.unimi.desm.plant;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import it.unimi.desm.common.PlantInfo;
import it.unimi.desm.grpc.Ack;
import it.unimi.desm.grpc.NewPlant;
import it.unimi.desm.grpc.PlantServiceGrpc;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import java.util.concurrent.TimeUnit;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ThermalPlantMain {

    private static final Gson gson = new Gson();

    // local state
    private static final List<PlantInfo> knownPlants = new ArrayList<>();
    private static String myId;
    private static int myGrpcPort;
    private static MqttClient mqttClient;

    public static void main(String[] args) throws IOException, InterruptedException, MqttException {
        if (args.length < 4) {
            System.err.println("Usage: ThermalPlantMain <id> <grpcPort> <adminHost> <adminPort>");
            System.exit(1);
        }

        myId = args[0];
        myGrpcPort = Integer.parseInt(args[1]);
        String adminHost = args[2];
        int adminPort = Integer.parseInt(args[3]);

        System.out.printf("Starting plant id=%s, grpcPort=%d, admin=%s:%d%n",
                myId, myGrpcPort, adminHost, adminPort);

        // 1. Start gRPC server first, so others can contact us
        Server grpcServer = startGrpcServer(myGrpcPort);

        // 2. Register with AdminServer
        List<PlantInfo> plantsFromServer = registerWithAdminServer(myId, myGrpcPort, adminHost, adminPort);

        synchronized (knownPlants) {
            knownPlants.clear();
            knownPlants.addAll(plantsFromServer);
        }

        System.out.println("Registered successfully. Current plants in the system:");
        synchronized (knownPlants) {
            for (PlantInfo p : knownPlants) {
                System.out.println("  - " + p);
            }
        }

        // 3. Notify existing plants about *me* (excluding myself)
        notifyOthersAboutMe();

        startMqttClient();

        System.out.println("ThermalPlant is now idle. Press Control+C to stop.");

        // keep process alive
        grpcServer.awaitTermination();
    }

    private static Server startGrpcServer(int port) throws IOException {
        Server server = ServerBuilder.forPort(port)
                .addService(new PlantServiceImpl())
                .build()
                .start();

        System.out.println("gRPC server started on port " + port);
        return server;
    }

    private static List<PlantInfo> registerWithAdminServer(
            String id, int grpcPort, String adminHost, int adminPort)
            throws IOException, InterruptedException {

        HttpClient client = HttpClient.newHttpClient();

        String url = String.format("http://%s:%d/plants", adminHost, adminPort);

        String jsonBody = gson.toJson(new PlantRegistrationRequest(id, "localhost", grpcPort));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to register plant: status " + response.statusCode()
                    + " body: " + response.body());
        }

        Type listType = new TypeToken<List<PlantInfo>>() {}.getType();
        return gson.fromJson(response.body(), listType);
    }

    private static class PlantRegistrationRequest {
        String id;
        String host;
        int grpcPort;

        PlantRegistrationRequest(String id, String host, int grpcPort) {
            this.id = id;
            this.host = host;
            this.grpcPort = grpcPort;
        }
    }

    // --- gRPC service implementation ---

    private static class PlantServiceImpl extends PlantServiceGrpc.PlantServiceImplBase {

        @Override
        public void notifyNewPlant(NewPlant request, StreamObserver<Ack> responseObserver) {
            PlantInfo p = new PlantInfo(request.getId(), request.getHost(), request.getGrpcPort());
            synchronized (knownPlants) {
                System.out.println("Received NotifyNewPlant: " + p);
                knownPlants.add(p);
            }
            Ack ack = Ack.newBuilder().setOk(true).build();
            responseObserver.onNext(ack);
            responseObserver.onCompleted();
        }
    }

    // --- client-side helper to notify others ---

    private static void notifyOthersAboutMe() {
        List<PlantInfo> snapshot;
        synchronized (knownPlants) {
            snapshot = new ArrayList<>(knownPlants);
        }

        for (PlantInfo other : snapshot) {
            // don't notify self
            if (other.getId().equals(myId)) {
                continue;
            }
            notifySinglePlant(other);
        }
    }
    private static void notifySinglePlant(PlantInfo other) {
        System.out.println("Notifying plant " + other.getId() + " about myself (" + myId + ")");

        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(other.getHost(), other.getGrpcPort())
                .usePlaintext()
                .build();

        PlantServiceGrpc.PlantServiceBlockingStub stub = PlantServiceGrpc.newBlockingStub(channel);

        NewPlant msg = NewPlant.newBuilder()
                .setId(myId)
                .setHost("localhost")
                .setGrpcPort(myGrpcPort)
                .build();

        try {
            Ack ack = stub.notifyNewPlant(msg);
            if (ack.getOk()) {
                System.out.println("Successfully notified plant " + other.getId());
            } else {
                System.err.println("Plant " + other.getId() + " returned ok=false");
            }
        } catch (StatusRuntimeException e) {
            System.err.println("Failed to notify plant " + other.getId() + ": " + e.getStatus());
        } catch (Exception e) {
            System.err.println("Failed to notify plant " + other.getId() + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            channel.shutdown();
            try {
                if (!channel.awaitTermination(1, TimeUnit.SECONDS)) {
                    channel.shutdownNow();
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                channel.shutdownNow();
            }
        }
    }

    private static void startMqttClient() throws MqttException {
        String broker = "tcp://localhost:1883";
        String clientId = "plant-" + myId;
        String topic = "desm/requests";

        mqttClient = new MqttClient(broker, clientId);
        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                System.err.println("MQTT connection lost for " + myId + ": " + cause);
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                String payload = new String(message.getPayload(), java.nio.charset.StandardCharsets.UTF_8);
                System.out.printf("Plant %s received MQTT on %s: %s%n", myId, topic, payload);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                // subscriber: nothing to do
            }
        });

        mqttClient.connect();
        mqttClient.subscribe(topic, 1);
        System.out.println("Plant " + myId + " subscribed to MQTT topic " + topic);
    }
}
