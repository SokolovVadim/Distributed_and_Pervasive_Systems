package it.unimi.desm.common;

public class PlantInfo {
    private final String id;
    private final String host;
    private final int grpcPort;

    public PlantInfo(String id, String host, int grpcPort) {
        this.id = id;
        this.host = host;
        this.grpcPort = grpcPort;
    }

    public String getId() {
        return id;
    }

    public String getHost() {
        return host;
    }

    public int getGrpcPort() {
        return grpcPort;
    }

    @Override
    public String toString() {
        return "PlantInfo{" +
                "id='" + id + '\'' +
                ", host='" + host + '\'' +
                ", grpcPort=" + grpcPort +
                '}';
    }
}
