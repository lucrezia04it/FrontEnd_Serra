package serra.autenticazione;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Scanner;

public class UserMenu {

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/server";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "Lucre04!";

    public static void start() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\n--- MENU UTENTE ---");
            System.out.println("1. Visualizza dati");
            System.out.println("2. Filtra per giorno");
            System.out.println("3. Filtra per temperatura");
            System.out.println("4. Esci");
            System.out.print("Scelta: ");
            String scelta = scanner.nextLine();

            switch (scelta) {
                case "1":
                    visualizzaDati();
                    break;
                case "2":
                    filtraPerGiorno(scanner);
                    break;
                case "3":
                    filtraPerTemperatura(scanner);
                    break;
                case "4":
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

    private static void filtraPerGiorno(Scanner scanner) {
        String query = "SELECT * FROM temperature_log WHERE timestamp::date = ? ORDER BY timestamp DESC";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                PreparedStatement stmt = conn.prepareStatement(query)) {

            System.out.print("Inserisci la data (yyyy-mm-dd): ");
            String dataInput = scanner.nextLine();
            Date data = Date.valueOf(dataInput); // java.sql.Date

            stmt.setDate(1, data);
            ResultSet rs = stmt.executeQuery();

            System.out.println("\n--- Dati del giorno " + data + " ---");
            while (rs.next()) {
                double temperatura = rs.getDouble("temperatura");
                double umidita = rs.getDouble("umidita");
                Timestamp timestamp = rs.getTimestamp("timestamp");

                LocalDateTime dateTime = timestamp.toLocalDateTime();
                String ora = dateTime.toLocalTime().withNano(0).toString();

                System.out.printf("Temp: %.2f°C | Umidità: %.2f%% | Ora: %s\n", temperatura, umidita, ora);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Errore durante il filtro per giorno.");
        }
    }

    private static void filtraPerTemperatura(Scanner scanner) {
        System.out.print("Inserisci soglia temperatura: ");
        double soglia = Double.parseDouble(scanner.nextLine());

        System.out.print("Vuoi filtrare (1) sopra o (2) sotto questa soglia? ");
        String opzione = scanner.nextLine();

        String operatore;
        if (opzione.equals("1")) {
            operatore = ">";
        } else if (opzione.equals("2")) {
            operatore = "<";
        } else {
            System.out.println("Scelta non valida.");
            return;
        }

        String query = "SELECT * FROM temperature_log WHERE temperatura " + operatore + " ? ORDER BY timestamp DESC";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setDouble(1, soglia);
            ResultSet rs = stmt.executeQuery();

            System.out.println("\n--- Letture con temperatura " + operatore + " " + soglia + "°C ---");
            while (rs.next()) {
                double temperatura = rs.getDouble("temperatura");
                double umidita = rs.getDouble("umidita");
                Timestamp timestamp = rs.getTimestamp("timestamp");

                System.out.printf("Temp: %.2f°C | Umidità: %.2f%% | Timestamp: %s\n",
                        temperatura, umidita, timestamp);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Errore durante il filtro per temperatura.");
        }
    }
}
