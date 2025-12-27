package it.unimi.desm.provider;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.charset.StandardCharsets;

public class ProviderMain {

    public static void main(String[] args) throws MqttException {
        // local Mosquitto
        String broker = "tcp://localhost:1883";
        String clientId = "provider-test";
        String topic = "desm/requests";

        MqttClient client = new MqttClient(broker, clientId);
        client.connect();

        String payload = "test-request-" + System.currentTimeMillis();
        MqttMessage message = new MqttMessage(payload.getBytes(StandardCharsets.UTF_8));
        message.setQos(1);

        client.publish(topic, message);
        System.out.println("Published MQTT message to " + topic + ": " + payload);

        client.disconnect();
        client.close();
    }
}
