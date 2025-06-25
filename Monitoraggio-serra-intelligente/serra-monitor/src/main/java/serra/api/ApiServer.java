package serra.api;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.*;
import java.util.ArrayList;
import java.util.Locale;
import java.util.List;

public class ApiServer {

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/server";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "Lucre04!";

    public static void start() throws Exception {
        // Ascolta su 0.0.0.0 per essere accessibile da altri dispositivi nella rete
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", 8080), 0);

        // Ho cambiato il contesto da /api/dati a /api/sensor_readings per farlo
        // corrispondere al frontend Vue
        // Se vuoi mantenere /api/dati nel backend, allora dovrai aggiornare l'URL nel
        // frontend Vue
        server.createContext("/api/sensor_readings", new DatiHandler());
        server.createContext("/api/login", new LoginHandler());

        server.setExecutor(null); // Utilizza il pool di thread di default
        server.start();
        System.out.println("API server avviato su http://localhost:8080/"); // Aggiornato messaggio
    }

    static class DatiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) {
            try {
                // --- INIZIO: Gestione Intestazioni CORS (devono essere sempre presenti) ---
                // PER TEST: Permetti tutte le origini (ATTENZIONE: Meno sicuro in produzione)
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                // Permetti i metodi che il frontend userà, inclusa OPTIONS per il preflight
                exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
                // Permetti le intestazioni che il frontend potrebbe inviare (es. Content-Type)
                exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
                // --- FINE: Gestione Intestazioni CORS ---

                // --- GESTIONE RICHIESTE PREFLIGHT (OPTIONS) ---
                if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                    exchange.sendResponseHeaders(204, -1); // 204 No Content, -1 per nessuna lunghezza di risposta
                    return; // Termina la richiesta OPTIONS qui
                }

                // --- GESTIONE RICHIESTE GET ---
                if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                    exchange.sendResponseHeaders(405, -1); // Method Not Allowed
                    return;
                }

                List<String> dati = new ArrayList<>();

                try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                        Statement stmt = conn.createStatement();
                        ResultSet rs = stmt
                                .executeQuery("SELECT * FROM temperature_log ORDER BY timestamp DESC LIMIT 50")) {

                    while (rs.next()) {
                        int id = rs.getInt("id");
                        double temp = rs.getDouble("temperatura");
                        double umid = rs.getDouble("umidita");
                        Timestamp ts = rs.getTimestamp("timestamp");

                        dati.add(String.format(Locale.US,
                                "{\"id\":%d,\"temperatura\":%.2f,\"umidita\":%.2f,\"timestamp\":\"%s\"}",
                                id, temp, umid, ts.toString() // Usa toString() per il timestamp
                        ));
                    }
                }

                String json = "[" + String.join(",", dati) + "]";

                // Imposta il Content-Type per la risposta JSON
                exchange.getResponseHeaders().set("Content-Type", "application/json");

                // Invia le intestazioni di risposta (incluso Content-Type e CORS) e la
                // dimensione
                exchange.sendResponseHeaders(200, json.getBytes().length);

                OutputStream os = exchange.getResponseBody();
                os.write(json.getBytes());
                os.close();

            } catch (Exception e) {
                e.printStackTrace();
                try {
                    exchange.sendResponseHeaders(500, -1); // Internal Server Error
                } catch (Exception ignored) {
                }
            }
        }
    }

    // Punto di avvio del server
    public static void main(String[] args) {
        try {
            // Assicurati che il driver JDBC di PostgreSQL sia nel classpath
            Class.forName("org.postgresql.Driver");

            start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}