package serra.monitoraggio;

import org.eclipse.paho.client.mqttv3.*;
import org.json.JSONObject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class SerraTemperatureSubscriber {

    private static final String BROKER = "tcp://localhost:1883";
    private static final String TOPIC = "server/monitoraggio";
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/server";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "Lucre04!";

    public static void main(String[] args) {
        try {
            MqttClient client = new MqttClient(BROKER, MqttClient.generateClientId(), new MemoryPersistence());
            client.connect();
            System.out.println("Subscriber connesso al broker MQTT");

            client.subscribe(TOPIC, (topic, msg) -> {
                String payload = new String(msg.getPayload());
                System.out.println("Ricevuto: " + payload);
                insertIntoDatabase(payload);
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void insertIntoDatabase(String jsonPayload) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            JSONObject json = new JSONObject(jsonPayload);

            if (json.has("temperatura") && json.has("umidita") && json.has("timestamp")) {
                double temperatura = json.getDouble("temperatura");
                double umidita = json.getDouble("umidita");
                long timestamp = json.getLong("timestamp");

                String query = "INSERT INTO temperature_log (temperatura, umidita, timestamp) VALUES (?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setDouble(1, temperatura);
                    stmt.setDouble(2, umidita);
                    stmt.setTimestamp(3, new java.sql.Timestamp(timestamp));
                    stmt.executeUpdate();
                }
            } else {
                System.err.println("⚠ JSON non valido o incompleto: " + jsonPayload);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
