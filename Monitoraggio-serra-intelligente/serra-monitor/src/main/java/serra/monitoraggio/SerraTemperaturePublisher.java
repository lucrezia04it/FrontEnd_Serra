package serra.monitoraggio;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Locale;

public class SerraTemperaturePublisher {

    private static final String BROKER = "tcp://localhost:1883";
    private static final String CLIENT_ID = "SerraTempClientCSV";
    private static final String TOPIC = "server/temperatura";

    public static void main(String[] args) {
        try {
            MqttClient client = new MqttClient(BROKER, CLIENT_ID, new MemoryPersistence());
            client.connect();
            System.out.println("Connesso al broker MQTT");

            BufferedReader reader = new BufferedReader(new FileReader("dati.csv"));
            String line;

            // Salta intestazione CSV
            reader.readLine();

            while ((line = reader.readLine()) != null) {
                String[] valori = line.split(",");
                if (valori.length < 2)
                    continue;

                double temperatura = Double.parseDouble(valori[0]);
                double umidita = Double.parseDouble(valori[1]);

                String payload = String.format(Locale.US,
                        "{\"temperatura\": %.2f, \"umidita\": %.2f, \"timestamp\": %d}",
                        temperatura, umidita, System.currentTimeMillis());

                MqttMessage message = new MqttMessage(payload.getBytes());
                message.setQos(0);

                client.publish(TOPIC, message);
                System.out.println("Inviato: " + payload);

                Thread.sleep(5000); // attesa di 5 secondi
            }

            reader.close();
            client.disconnect();
            System.out.println("Disconnesso dal broker MQTT");

        } catch (MqttException | IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}