package serra.monitoraggio;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.*;
import java.util.Locale;

public class SerraTemperaturePublisherCSV {

    public static void main(String[] args) {
        String broker = "tcp://localhost:1883";
        String clientId = "publisher_csv";
        String topic = "server/monitoraggio";

        try {
            MqttClient client = new MqttClient(broker, clientId, new MemoryPersistence());
            client.connect();

            System.out.println("Connesso al broker MQTT");

            BufferedReader reader = new BufferedReader(new FileReader("dati.csv"));
            String line;

            // Salta intestazione
            reader.readLine();

            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                double temperatura = Double.parseDouble(values[0]);
                double umidita = Double.parseDouble(values[1]);

                String payload = String.format(Locale.US,
                        "{\"temperatura\": %.2f, \"umidita\": %.2f, \"timestamp\": %d}",
                        temperatura, umidita, System.currentTimeMillis());

                MqttMessage message = new MqttMessage(payload.getBytes());
                client.publish(topic, message);

                System.out.println("Inviato: " + payload);
                Thread.sleep(3000); // ogni 3 secondi
            }

            client.disconnect();
            reader.close();
            System.out.println("Disconnesso dal broker");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}