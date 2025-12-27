package it.unimi.desm.plant;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import it.unimi.desm.common.PlantInfo;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ThermalPlantMain {

    private static final Gson gson = new Gson();

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length < 4) {
            System.err.println("Usage: ThermalPlantMain <id> <grpcPort> <adminHost> <adminPort>");
            System.exit(1);
        }

        String id = args[0];
        int grpcPort = Integer.parseInt(args[1]);
        String adminHost = args[2];
        int adminPort = Integer.parseInt(args[3]);

        System.out.printf("Starting plant id=%s, grpcPort=%d, admin=%s:%d%n", id, grpcPort, adminHost, adminPort);

        List<PlantInfo> plants = registerWithAdminServer(id, grpcPort, adminHost, adminPort);

        System.out.println("Registered successfully. Current plants in the system:");
        for (PlantInfo p : plants) {
            System.out.println("  - " + p);
        }

        // TODO next: start gRPC server & MQTT, just keep the process alive
        System.out.println("ThermalPlant is now idle. Press Control+C to stop.");
        // simple keep ot alive loop
        while (true) {
            Thread.sleep(10_000);
        }
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
}
