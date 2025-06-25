package serra.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.io.ByteArrayOutputStream;
import java.sql.*;

public class LoginHandler implements HttpHandler {

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/server";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "Lucre04!";

    @Override
    public void handle(HttpExchange exchange) {
        try {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            InputStream is = exchange.getRequestBody();
            String requestBody = new String(readAllBytes(is), StandardCharsets.UTF_8);

            String username = extractJsonValue(requestBody, "username");
            String password = extractJsonValue(requestBody, "password");

            if (username == null || password == null) {
                sendResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"Missing username or password\"}");
                return;
            }

            // Verifica credenziali nel DB
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                String sql = "SELECT id, role FROM users WHERE username = ? AND password = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, username);
                    stmt.setString(2, password);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            int id = rs.getInt("id");
                            String role = rs.getString("role");

                            // String json = String.format(
                            // "{\"status\":\"ok\",\"message\":\"Login
                            // successful\",\"userId\":%d,\"role\":\"%s\"}",
                            // id, role);
                            String json = String.format(
                                    "{\"success\":true,\"message\":\"Login ok\",\"id\":%d,\"username\":\"%s\",\"role\":\"%s\"}",
                                    id, username, role);
                            sendResponse(exchange, 200, json);
                        } else {
                            sendResponse(exchange, 401, "{\"status\":\"error\",\"message\":\"Invalid credentials\"}");
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            try {
                exchange.sendResponseHeaders(500, -1);
            } catch (Exception ignored) {
            }
        }
    }

    private byte[] readAllBytes(InputStream is) throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }

    private String extractJsonValue(String json, String key) {
        String pattern = "\"" + key + "\"";
        int idx = json.indexOf(pattern);
        if (idx == -1)
            return null;

        int colonIdx = json.indexOf(":", idx);
        if (colonIdx == -1)
            return null;

        int startQuote = json.indexOf("\"", colonIdx);
        if (startQuote == -1)
            return null;

        int endQuote = json.indexOf("\"", startQuote + 1);
        if (endQuote == -1)
            return null;

        return json.substring(startQuote + 1, endQuote);
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String responseJson) throws Exception {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] bytes = responseJson.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
}
