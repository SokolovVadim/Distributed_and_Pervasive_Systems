package it.unimi.desm.adminserver;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import it.unimi.desm.common.PlantInfo;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class AdminServerMain {

    private static final int DEFAULT_PORT = 8080;
    private static final Gson gson = new Gson();

    public static void main(String[] args) throws IOException {
        int port = DEFAULT_PORT;
        if (args.length >= 1) {
            port = Integer.parseInt(args[0]);
        }

        PlantRegistry registry = new PlantRegistry();

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        System.out.println("AdminServer listening on port " + port);

        // POST /plants, then register plant, returns list of plants
        server.createContext("/plants", exchange -> handlePlants(exchange, registry));

        // default executor
        server.setExecutor(null);
        server.start();
    }

    private static void handlePlants(HttpExchange exchange, PlantRegistry registry) throws IOException {
        String method = exchange.getRequestMethod();

        try {
            if ("POST".equalsIgnoreCase(method)) {
                handleRegisterPlant(exchange, registry);
            } else if ("GET".equalsIgnoreCase(method)) {
                handleListPlants(exchange, registry);
            } else {
                // Method Not Allowed
                exchange.sendResponseHeaders(405, -1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            byte[] resp = ("{\"error\":\"" + e.getMessage() + "\"}").getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(500, resp.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(resp);
            }
        }
    }

    private static void handleRegisterPlant(HttpExchange exchange, PlantRegistry registry) throws IOException {
        JsonObject body = gson.fromJson(
                new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8),
                JsonObject.class
        );

        String id = body.get("id").getAsString();
        String host = body.get("host").getAsString();
        int grpcPort = body.get("grpcPort").getAsInt();

        PlantInfo newPlant = new PlantInfo(id, host, grpcPort);

        List<PlantInfo> allPlants = registry.registerPlant(newPlant);

        String json = gson.toJson(allPlants);
        byte[] response = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }

    private static void handleListPlants(HttpExchange exchange, PlantRegistry registry) throws IOException {
        List<PlantInfo> allPlants = registry.listPlants();
        String json = gson.toJson(allPlants);
        byte[] response = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }
}
