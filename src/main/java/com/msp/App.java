package com.msp;

import com.msp.model.SparePart;
import com.msp.service.AuthenticationService;
import com.msp.service.SparePartService;
import com.msp.service.UserService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class App {

    private static final int PORT =
            Integer.getInteger("msp.port", 8080);

    private static final UserService userService =
            new UserService();

    private static final AuthenticationService authenticationService =
            new AuthenticationService(userService);

    private static final SparePartService sparePartService =
            new SparePartService();

    public static void main(String[] args) throws IOException {

        HttpServer server =
                HttpServer.create(new InetSocketAddress(PORT), 0);

        // API endpoints
        server.createContext("/api/register", App::handleRegister);
        server.createContext("/api/login", App::handleLogin);
        server.createContext("/api/spare-parts", App::handleSpareParts);
        server.createContext("/api/models", App::handleModels);

        // Frontend
        server.createContext("/", App::handleStatic);

        server.setExecutor(null);
        server.start();

        System.out.println(
                "Mobile Spare Parts Management System running at http://localhost:"
                        + PORT
        );

        System.out.println("API: GET /api/spare-parts");
        System.out.println("API: GET /api/models");
        System.out.println("Press Ctrl+C to stop.");
    }

    // =========================
    // REGISTER API
    // =========================

    private static void handleRegister(HttpExchange exchange)
            throws IOException {

        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            send(exchange, 405,
                    "{\"error\":\"Method not allowed\"}");
            return;
        }

        Map<String, String> form = parseForm(exchange);

        try {

            boolean created = userService.register(
                    form.get("username"),
                    form.get("password"),
                    form.get("email")
            );

            if (!created) {
                send(exchange, 409,
                        "{\"success\":false,\"message\":\"Username already exists\"}");
                return;
            }

            send(exchange, 201,
                    "{\"success\":true,\"message\":\"Registration successful\"}");

        } catch (IllegalArgumentException ex) {

            send(exchange, 400,
                    jsonError(ex.getMessage()));
        }
    }

    // =========================
    // LOGIN API
    // =========================

    private static void handleLogin(HttpExchange exchange)
            throws IOException {

        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            send(exchange, 405,
                    "{\"error\":\"Method not allowed\"}");
            return;
        }

        Map<String, String> form = parseForm(exchange);

        boolean authenticated =
                authenticationService.authenticate(
                        form.get("username"),
                        form.get("password")
                );

        if (authenticated) {

            send(exchange, 200,
                    "{\"success\":true,\"message\":\"Login successful\"}");

        } else {

            send(exchange, 401,
                    "{\"success\":false,\"message\":\"Invalid username or password\"}");
        }
    }

    // =========================
    // MSPR-24
    // GET SPARE PARTS API
    // =========================

    private static void handleSpareParts(HttpExchange exchange)
            throws IOException {

        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {

            send(exchange, 405,
                    "{\"error\":\"Method not allowed\"}");

            return;
        }

        StringBuilder json = new StringBuilder("[");

        boolean first = true;

        for (SparePart part : sparePartService.getAllParts()) {

            if (!first) {
                json.append(',');
            }

            first = false;

            json.append("{")
                    .append("\"id\":")
                    .append(part.getId())
                    .append(',')

                    .append("\"partName\":\"")
                    .append(escape(part.getPartName()))
                    .append("\",")

                    .append("\"model\":\"")
                    .append(escape(part.getModel()))
                    .append("\",")

                    .append("\"quantity\":")
                    .append(part.getQuantity())
                    .append(',')

                    .append("\"price\":")
                    .append(String.format(
                            java.util.Locale.US,
                            "%.2f",
                            part.getPrice()
                    ))
                    .append(',')

                    .append("\"available\":")
                    .append(part.getQuantity() > 0)

                    .append("}");
        }

        json.append(']');

        send(exchange, 200, json.toString());
    }

    // =========================
    // MODELS API
    // =========================

    private static void handleModels(HttpExchange exchange)
            throws IOException {

        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {

            send(exchange, 405,
                    "{\"error\":\"Method not allowed\"}");

            return;
        }

        StringBuilder json = new StringBuilder("[");

        boolean first = true;

        for (SparePart part : sparePartService.getAllParts()) {

            if (!first) {
                json.append(',');
            }

            first = false;

            json.append("\"")
                    .append(escape(part.getModel()))
                    .append("\"");
        }

        json.append(']');

        send(exchange, 200, json.toString());
    }

    // =========================
    // STATIC FRONTEND
    // =========================

    private static void handleStatic(HttpExchange exchange)
            throws IOException {

        URI uri = exchange.getRequestURI();

        String requested = uri.getPath();

        if ("/".equals(requested)) {
            requested = "/index.html";
        }

        if (requested.contains("..")) {

            send(exchange, 400,
                    "Invalid path");

            return;
        }

        Path file =
                Path.of(
                        "frontend",
                        requested.substring(1)
                );

        if (!Files.exists(file) || Files.isDirectory(file)) {

            send(exchange, 404,
                    "Page not found");

            return;
        }

        String contentType =
                contentType(file);

        byte[] data =
                Files.readAllBytes(file);

        exchange.getResponseHeaders()
                .set("Content-Type", contentType);

        exchange.sendResponseHeaders(
                200,
                data.length
        );

        try (OutputStream out =
                     exchange.getResponseBody()) {

            out.write(data);
        }
    }

    // =========================
    // FORM PARSER
    // =========================

    private static Map<String, String> parseForm(
            HttpExchange exchange)
            throws IOException {

        String body =
                new String(
                        exchange.getRequestBody().readAllBytes(),
                        StandardCharsets.UTF_8
                );

        Map<String, String> result =
                new HashMap<>();

        for (String pair : body.split("&")) {

            if (pair.isBlank()) {
                continue;
            }

            String[] parts =
                    pair.split("=", 2);

            String key =
                    URLDecoder.decode(
                            parts[0],
                            StandardCharsets.UTF_8
                    );

            String value =
                    parts.length > 1
                            ? URLDecoder.decode(
                                    parts[1],
                                    StandardCharsets.UTF_8
                            )
                            : "";

            result.put(key, value);
        }

        return result;
    }

    // =========================
    // CONTENT TYPE
    // =========================

    private static String contentType(Path file) {

        String name =
                file.getFileName()
                        .toString()
                        .toLowerCase();

        if (name.endsWith(".html")) {
            return "text/html; charset=UTF-8";
        }

        if (name.endsWith(".css")) {
            return "text/css; charset=UTF-8";
        }

        if (name.endsWith(".js")) {
            return "application/javascript; charset=UTF-8";
        }

        return "application/octet-stream";
    }

    // =========================
    // JSON ERROR
    // =========================

    private static String jsonError(String message) {

        return "{\"success\":false,\"message\":\""
                + escape(message)
                + "\"}";
    }

    // =========================
    // JSON ESCAPE
    // =========================

    private static String escape(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    // =========================
    // SEND RESPONSE
    // =========================

    private static void send(
            HttpExchange exchange,
            int status,
            String body)
            throws IOException {

        byte[] data =
                body.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders()
                .set(
                        "Content-Type",
                        "application/json; charset=UTF-8"
                );

        exchange.sendResponseHeaders(
                status,
                data.length
        );

        try (OutputStream out =
                     exchange.getResponseBody()) {

            out.write(data);
        }
    }
}