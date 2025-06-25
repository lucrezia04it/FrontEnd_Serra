package serra.autenticazione;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.Scanner;
import org.json.JSONArray;
import org.json.JSONObject;

public class AdminMenu {

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/server";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "Lucre04!";

    public static void start() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\n--- MENU ADMIN ---");
            System.out.println("1. Visualizza dati");
            System.out.println("2. Aggiungi sensore (simulato)");
            System.out.println("3. Elimina sensore (simulato)");
            System.out.println("4. Esporta dati in CSV");
            System.out.println("5. Esporta dati in JSON");
            System.out.println("6. Esci");
            System.out.print("Scelta: ");
            String scelta = scanner.nextLine();

            switch (scelta) {
                case "1":
                    visualizzaDati();
                    break;
                case "2":
                    System.out.println("Simulazione aggiunta sensore.");
                    break;
                case "3":
                    System.out.println("Simulazione eliminazione sensore.");
                    break;
                case "4":
                    esportaCSV("dati_esportati.csv");
                    break;
                case "5":
                    esportaJSON("dati_esportati.json");
                    break;
                case "6":
                    return;
                default:
                    System.out.println("Scelta non valida.");
            }
        }
    }

    private static void visualizzaDati() {
        String query = "SELECT * FROM temperature_log ORDER BY timestamp DESC LIMIT 50";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            System.out.println("\n--- Ultime letture sensori ---");
            while (rs.next()) {
                int id = rs.getInt("id");
                double temperatura = rs.getDouble("temperatura");
                double umidita = rs.getDouble("umidita");
                Timestamp timestamp = rs.getTimestamp("timestamp");

                System.out.printf("ID: %d | Temp: %.2f°C | Umidità: %.2f%% | Timestamp: %s\n",
                        id, temperatura, umidita, timestamp);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Errore durante la lettura dei dati.");
        }
    }

    private static void esportaCSV(String filename) {
        String query = "SELECT * FROM temperature_log";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                FileWriter writer = new FileWriter(filename)) {

            writer.write("id,temperatura,umidita,timestamp\n");

            while (rs.next()) {
                writer.write(String.format("%d,%.2f,%.2f,%s\n",
                        rs.getInt("id"),
                        rs.getDouble("temperatura"),
                        rs.getDouble("umidita"),
                        rs.getTimestamp("timestamp").toString()));
            }

            System.out.println("Dati esportati correttamente in " + filename);
        } catch (IOException | SQLException e) {
            e.printStackTrace();
            System.out.println("Errore durante l'esportazione in CSV.");
        }
    }

    private static void esportaJSON(String filename) {
        String query = "SELECT * FROM temperature_log";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                FileWriter writer = new FileWriter(filename)) {

            JSONArray jsonArray = new JSONArray();

            while (rs.next()) {
                JSONObject obj = new JSONObject();
                obj.put("id", rs.getInt("id"));
                obj.put("temperatura", rs.getDouble("temperatura"));
                obj.put("umidita", rs.getDouble("umidita"));
                obj.put("timestamp", rs.getTimestamp("timestamp").toString());
                jsonArray.put(obj);
            }

            writer.write(jsonArray.toString(4));
            System.out.println("Dati esportati correttamente in " + filename);
        } catch (IOException | SQLException e) {
            e.printStackTrace();
            System.out.println("Errore durante l'esportazione in JSON.");
        }
    }
}
