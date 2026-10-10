package com.msp;

import com.msp.model.SparePart;
import com.msp.model.Review;
import com.msp.service.AuthenticationService;
import com.msp.service.ModelService;
import com.msp.service.OrderService;
import com.msp.service.PriceTrackingService;
import com.msp.service.RecommendationService;
import com.msp.service.RepairGuideService;
import com.msp.service.ReturnService;
import com.msp.service.SparePartService;
import com.msp.service.ReviewService;
import com.msp.service.UserService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class App {
    private static final int PORT = Integer.getInteger("PORT", 8080);
    private static final Path FRONTEND_DIR = Paths.get("frontend").toAbsolutePath().normalize();

    private static final UserService USER_SERVICE = new UserService();
    private static final ModelService MODEL_SERVICE = new ModelService();
    private static final SparePartService SPARE_PART_SERVICE = new SparePartService(MODEL_SERVICE);
    private static final OrderService ORDER_SERVICE = new OrderService();
    private static final ReviewService REVIEW_SERVICE = new ReviewService();
    private static final PriceTrackingService PRICE_TRACKING_SERVICE = new PriceTrackingService();
    private static final RecommendationService RECOMMENDATION_SERVICE = new RecommendationService(SPARE_PART_SERVICE);
    private static final RepairGuideService REPAIR_GUIDE_SERVICE = new RepairGuideService();
    private static final ReturnService RETURN_SERVICE = new ReturnService();
    private static final AuthenticationService AUTH_SERVICE = new AuthenticationService(USER_SERVICE);
    private static final Map<Integer, Map<String, Object>> PRICE_TRACKING_STORE = new ConcurrentHashMap<>();
    private static final List<Map<String, Object>> NOTIFICATIONS = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/", new StaticFileHandler());
        server.createContext("/api", new ApiHandler());
        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();
        System.out.println("PartPulse MSP running at http://localhost:" + PORT);
    }

    private static final class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendCors(exchange, 200, "");
                return;
            }

            String requestPath = normalizeRequestPath(exchange.getRequestURI());
            if (requestPath == null || requestPath.isBlank() || "/".equals(requestPath)) {
                requestPath = "/index.html";
            }

            Path target = FRONTEND_DIR.resolve(requestPath.substring(1)).normalize();
            if (!target.startsWith(FRONTEND_DIR) || !Files.exists(target) || !Files.isRegularFile(target)) {
                if (requestPath.endsWith("/")) {
                    target = FRONTEND_DIR.resolve("index.html").normalize();
                } else {
                    sendError(exchange, 404, "Page not found");
                    return;
                }
            }

            byte[] content = Files.readAllBytes(target);
            exchange.getResponseHeaders().add("Content-Type", resolveMimeType(target.getFileName().toString()));
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Cache-Control", "no-cache");
            exchange.sendResponseHeaders(200, content.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(content);
            }
        }
    }

    private static final class ApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            if ("OPTIONS".equalsIgnoreCase(method)) {
                sendCors(exchange, 200, "");
                return;
            }

            try {
                String path = normalizeApiPath(exchange.getRequestURI());
                switch (path) {
                    case "/api/health":
                        sendJson(exchange, 200, Map.of(
                                "status", "UP",
                                "dataMode", "DEMO",
                                "timestamp", Instant.now().toString()));
                        return;
                    case "/api/brands":
                        sendJson(exchange, 200, MODEL_SERVICE.getBrands());
                        return;
                    case "/api/models":
                        sendJson(exchange, 200, getModelsPayload(exchange.getRequestURI()));
                        return;
                    case "/api/spare-parts":
                        sendJson(exchange, 200, getSparePartsPayload(exchange.getRequestURI()));
                        return;
                    case "/api/product":
                        sendJson(exchange, 200, getProductPayload(exchange.getRequestURI()));
                        return;
                    case "/api/recommendations":
                        sendJson(exchange, 200, RECOMMENDATION_SERVICE.recommend(
                                queryParam(exchange.getRequestURI(), "model"),
                                queryParam(exchange.getRequestURI(), "category")));
                        return;
                    case "/api/compatibility":
                        sendJson(exchange, 200, buildCompatibilityPayload(exchange.getRequestURI()));
                        return;
                    case "/api/reviews":
                        if ("POST".equalsIgnoreCase(method)) {
                            sendJson(exchange, 200, createReview(exchange));
                        } else {
                            sendJson(exchange, 200, getReviewsPayload(exchange.getRequestURI()));
                        }
                        return;
                    case "/api/auth/register":
                        if (!"POST".equalsIgnoreCase(method)) {
                            sendError(exchange, 405, "Method not allowed");
                            return;
                        }
                        sendJson(exchange, 200, registerUser(exchange));
                        return;
                    case "/api/auth/login":
                        if (!"POST".equalsIgnoreCase(method)) {
                            sendError(exchange, 405, "Method not allowed");
                            return;
                        }
                        sendJson(exchange, 200, authenticateUser(exchange));
                        return;
                    case "/api/orders":
                        if ("GET".equalsIgnoreCase(method)) {
                            sendJson(exchange, 200, ORDER_SERVICE.all());
                        } else if ("POST".equalsIgnoreCase(method)) {
                            sendError(exchange, 503,
                                    "Order creation is disabled in demo mode. No order was created.");
                        } else {
                            sendError(exchange, 405, "Method not allowed");
                        }
                        return;
                    case "/api/returns":
                        if ("POST".equalsIgnoreCase(method)) {
                            sendJson(exchange, 200, createReturn(exchange));
                        } else {
                            sendJson(exchange, 200, RETURN_SERVICE.all());
                        }
                        return;
                    case "/api/repair-guides":
                        sendJson(exchange, 200, REPAIR_GUIDE_SERVICE.guides(queryParam(exchange.getRequestURI(), "model")));
                        return;
                    case "/api/price-tracking":
                        if ("POST".equalsIgnoreCase(method)) {
                            sendJson(exchange, 200, createPriceTracking(exchange));
                        } else if ("DELETE".equalsIgnoreCase(method)) {
                            sendJson(exchange, 200, deletePriceTracking(exchange.getRequestURI()));
                        } else {
                            sendJson(exchange, 200, getPriceTrackingPayload(exchange));
                        }
                        return;
                    case "/api/cart":
                        if ("GET".equalsIgnoreCase(method)) {
                            sendJson(exchange, 200, List.of());
                        } else {
                            sendError(exchange, 503,
                                    "The server cart is not configured. Use the browser demo cart.");
                        }
                        return;
                    case "/api/notifications":
                        sendJson(exchange, 200, getNotifications());
                        return;
                    default:
                        sendError(exchange, 404, "Endpoint not found");
                        return;
                }
            } catch (IllegalArgumentException ex) {
                sendJson(exchange, 400, Map.of("error", ex.getMessage()));
            } catch (Exception ex) {
                sendJson(exchange, 500, Map.of("error", ex.getMessage()));
            }
        }
    }

    private static String normalizeRequestPath(URI uri) {
        String path = uri.getPath();
        if (path == null || path.isBlank()) {
            return "/";
        }
        return path.startsWith("/") ? path : "/" + path;
    }

    private static String normalizeApiPath(URI uri) {
        String path = normalizeRequestPath(uri);
        if (path.equals("/api") || path.equals("/api/")) {
            return "/api/health";
        }
        return path;
    }

    private static Map<String, Object> parseJsonBody(HttpExchange exchange) throws IOException {
        String raw = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8).trim();
        if (raw.isBlank() || "{}".equals(raw)) {
            return new HashMap<>();
        }
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (contentType != null && contentType.toLowerCase(Locale.ROOT)
                .startsWith("application/x-www-form-urlencoded")) {
            Map<String, Object> values = new LinkedHashMap<>();
            for (String pair : raw.split("&")) {
                String[] keyValue = pair.split("=", 2);
                String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                String value = keyValue.length == 2
                        ? URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8)
                        : "";
                values.put(key, value);
            }
            return values;
        }
        Matcher matcher = Pattern.compile("\\\"([^\\\"]+)\\\"\\s*:\\s*(\\\"((?:\\\\.|[^\\\"\\\\])*)\\\"|true|false|null|-?\\d+(?:\\.\\d+)?|\\{.*?\\}|\\[.*?\\])", Pattern.DOTALL)
                .matcher(raw);
        Map<String, Object> values = new LinkedHashMap<>();
        while (matcher.find()) {
            String key = matcher.group(1);
            String valueText = matcher.group(2).trim();
            values.put(key, parseJsonValue(valueText));
        }
        return values;
    }

    private static Object parseJsonValue(String valueText) {
        String trimmed = valueText.trim();
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
            return decodeJsonString(trimmed.substring(1, trimmed.length() - 1));
        }
        if ("true".equalsIgnoreCase(trimmed)) {
            return true;
        }
        if ("false".equalsIgnoreCase(trimmed)) {
            return false;
        }
        if ("null".equalsIgnoreCase(trimmed)) {
            return null;
        }
        if (trimmed.contains(".")) {
            return Double.parseDouble(trimmed);
        }
        if (trimmed.matches("-?\\d+")) {
            return Long.parseLong(trimmed);
        }
        return trimmed;
    }

    private static String decodeJsonString(String value) {
        return value.replace("\\\"", "\"")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\\\", "\\");
    }

    private static String queryParam(URI uri, String name) {
        if (uri == null || uri.getQuery() == null) {
            return null;
        }
        for (String pair : uri.getQuery().split("&")) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2 && name.equalsIgnoreCase(keyValue[0])) {
                try {
                    return URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                } catch (IllegalArgumentException ex) {
                    return keyValue[1];
                }
            }
        }
        return null;
    }

    private static List<Map<String, String>> getModelsPayload(URI uri) {
        String brand = queryParam(uri, "brand");
        return MODEL_SERVICE.getModelCatalog().stream()
                .filter(model -> brand == null || brand.isBlank()
                        || model.get("brand").equalsIgnoreCase(brand.trim()))
                .toList();
    }

    private static List<Map<String, Object>> getSparePartsPayload(URI uri) {
        String query = queryParam(uri, "query");
        String model = queryParam(uri, "model");
        String category = queryParam(uri, "category");
        return SPARE_PART_SERVICE.search(query, model, category).stream()
                .map(App::toSparePartMap)
                .toList();
    }

    private static Map<String, Object> getProductPayload(URI uri) {
        int id = parsePositiveInt(queryParam(uri, "id"), "id");
        SparePart part = SPARE_PART_SERVICE.findById(id);
        if (part == null) {
            return Map.of("found", false, "message", "Product not found");
        }
        return toSparePartMap(part);
    }

    private static List<Map<String, Object>> getReviewsPayload(URI uri) {
        String partId = queryParam(uri, "partId");
        if (partId == null || partId.isBlank()) {
            return REVIEW_SERVICE.getReviews(1, 1, "date").stream().map(App::toReviewMap).toList();
        }
        return REVIEW_SERVICE.getReviews(parsePositiveInt(partId, "partId"), 1, "date").stream().map(App::toReviewMap).toList();
    }

    private static Map<String, Object> createReview(HttpExchange exchange) throws IOException {
        Map<String, Object> payload = parseJsonBody(exchange);
        int partId = parsePositiveInt(String.valueOf(payload.getOrDefault("partId", 0)), "partId");
        String username = String.valueOf(payload.getOrDefault("username", "")).trim();
        int rating = Integer.parseInt(String.valueOf(payload.getOrDefault("rating", 0)));
        String comment = String.valueOf(payload.getOrDefault("comment", "")).trim();
        Review review = REVIEW_SERVICE.addReview(partId, username, rating, comment);
        return toReviewMap(review);
    }

    private static Map<String, Object> registerUser(HttpExchange exchange) throws IOException {
        Map<String, Object> payload = parseJsonBody(exchange);
        String username = safeString(payload.get("username"));
        String password = safeString(payload.get("password"));
        String email = safeString(payload.get("email"));
        boolean success = USER_SERVICE.register(username, password, email);
        if (!success) {
            throw new IllegalArgumentException("Username or email already exists");
        }
        return Map.of("success", true, "message", "Registration successful", "username", username, "email", email);
    }

    private static Map<String, Object> authenticateUser(HttpExchange exchange) throws IOException {
        Map<String, Object> payload = parseJsonBody(exchange);
        String username = safeString(payload.get("username"));
        String password = safeString(payload.get("password"));
        boolean success = AUTH_SERVICE.authenticate(username, password);
        if (!success) {
            throw new IllegalArgumentException("Invalid username or password");
        }
        return Map.of("success", true, "message", "Login successful", "username", username);
    }

    private static Map<String, Object> createReturn(HttpExchange exchange) throws IOException {
        Map<String, Object> payload = parseJsonBody(exchange);
        int orderId = parsePositiveInt(String.valueOf(payload.getOrDefault("orderId", 0)), "orderId");
        String reason = safeString(payload.get("reason"));
        Map<String, String> created = RETURN_SERVICE.create(orderId, reason);
        Map<String, Object> result = new LinkedHashMap<>();
        result.putAll(created);
        return result;
    }

    private static List<Map<String, Object>> getPriceTrackingPayload(HttpExchange exchange) {
        List<Map<String, Object>> all = new ArrayList<>();
        all.addAll(PRICE_TRACKING_SERVICE.prices(SPARE_PART_SERVICE.getAllParts()));
        all.addAll(PRICE_TRACKING_STORE.values());
        return all;
    }

    private static Map<String, Object> createPriceTracking(HttpExchange exchange) throws IOException {
        Map<String, Object> payload = parseJsonBody(exchange);
        int partId = parsePositiveInt(String.valueOf(payload.getOrDefault("partId", 0)), "partId");
        double targetPrice = parsePositiveDouble(String.valueOf(payload.getOrDefault("targetPrice", 0)), "targetPrice");
        int id = PRICE_TRACKING_STORE.isEmpty() ? 1 : Collections.max(PRICE_TRACKING_STORE.keySet()) + 1;
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", id);
        item.put("partId", partId);
        item.put("targetPrice", targetPrice);
        item.put("partName", SPARE_PART_SERVICE.findById(partId) != null ? SPARE_PART_SERVICE.findById(partId).getPartName() : "Unknown");
        PRICE_TRACKING_STORE.put(id, item);
        NOTIFICATIONS.add(Map.of("id", id, "message", "Price alert created for part #" + partId, "time", Instant.now().toString()));
        return item;
    }

    private static Map<String, Object> deletePriceTracking(URI uri) {
        String idParam = queryParam(uri, "id");
        if (idParam == null || idParam.isBlank()) {
            throw new IllegalArgumentException("Price tracking ID is required");
        }
        int id = parsePositiveInt(idParam, "id");
        Map<String, Object> removed = PRICE_TRACKING_STORE.remove(id);
        return Map.of("success", removed != null, "id", id, "removed", removed != null);
    }

    private static List<Map<String, Object>> getNotifications() {
        List<Map<String, Object>> list = new ArrayList<>(NOTIFICATIONS);
        if (list.isEmpty()) {
            list.add(Map.of("id", 1, "message", "Welcome back to PartPulse MSP", "time", Instant.now().toString()));
        }
        return list;
    }

    private static Map<String, Object> buildCompatibilityPayload(URI uri) {
        String partId = queryParam(uri, "partId");
        String model = queryParam(uri, "model");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("compatible", null);
        result.put("status", "unverified");
        result.put("verification", "unverified");
        result.put("message", "Compatibility has not been verified in demo mode.");
        if (partId != null && !partId.isBlank()) {
            result.put("partId", parsePositiveInt(partId, "partId"));
        }
        if (model != null && !model.isBlank()) {
            result.put("model", model);
        }
        return result;
    }

    private static Map<String, Object> toSparePartMap(SparePart part) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", part.getId());
        map.put("partName", part.getPartName());
        map.put("brand", MODEL_SERVICE.getBrandForModel(part.getModel()));
        map.put("model", part.getModel());
        map.put("quantity", part.getQuantity());
        map.put("price", part.getPrice());
        return map;
    }

    private static Map<String, Object> toReviewMap(Review review) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", review.getId());
        map.put("partId", review.getPartId());
        map.put("username", review.getUsername());
        map.put("rating", review.getRating());
        map.put("comment", review.getComment());
        map.put("date", review.getDate().toString());
        return map;
    }

    private static String safeString(Object value) {
        String text = value == null ? "" : String.valueOf(value).trim();
        if (text.isBlank()) {
            throw new IllegalArgumentException("Required field is missing");
        }
        return text;
    }

    private static int parsePositiveInt(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        try {
            int parsed = Integer.parseInt(value);
            if (parsed <= 0) {
                throw new IllegalArgumentException(fieldName + " must be greater than zero");
            }
            return parsed;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid " + fieldName + " value");
        }
    }

    private static double parsePositiveDouble(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        try {
            double parsed = Double.parseDouble(value);
            if (parsed < 0) {
                throw new IllegalArgumentException(fieldName + " cannot be negative");
            }
            return parsed;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid " + fieldName + " value");
        }
    }

    private static void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        sendJson(exchange, statusCode, Map.of("error", message));
    }

    private static void sendJson(HttpExchange exchange, int statusCode, Object payload) throws IOException {
        String body = toJson(payload);
        byte[] data = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
        exchange.sendResponseHeaders(statusCode, data.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(data);
        }
    }

    private static void sendCors(HttpExchange exchange, int statusCode, String body) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
        exchange.sendResponseHeaders(statusCode, body.getBytes(StandardCharsets.UTF_8).length);
        if (!body.isBlank()) {
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    private static String toJson(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof String text) {
            return "\"" + escapeJson(text) + "\"";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return String.valueOf(value);
        }
        if (value instanceof Map<?, ?> map) {
            StringJoiner joiner = new StringJoiner(",", "{", "}");
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                joiner.add("\"" + escapeJson(String.valueOf(entry.getKey())) + "\":" + toJson(entry.getValue()));
            }
            return joiner.toString();
        }
        if (value instanceof Iterable<?> iterable) {
            StringJoiner joiner = new StringJoiner(",", "[", "]");
            for (Object item : iterable) {
                joiner.add(toJson(item));
            }
            return joiner.toString();
        }
        if (value.getClass().isArray()) {
            StringJoiner joiner = new StringJoiner(",", "[", "]");
            Object[] array = (Object[]) value;
            for (Object item : array) {
                joiner.add(toJson(item));
            }
            return joiner.toString();
        }
        return "\"" + escapeJson(String.valueOf(value)) + "\"";
    }

    private static String escapeJson(String text) {
        StringBuilder builder = new StringBuilder();
        for (char ch : text.toCharArray()) {
            switch (ch) {
                case '\\' -> builder.append("\\\\");
                case '"' -> builder.append("\\\"");
                case '\n' -> builder.append("\\n");
                case '\r' -> builder.append("\\r");
                case '\t' -> builder.append("\\t");
                case '\b' -> builder.append("\\b");
                case '\f' -> builder.append("\\f");
                default -> builder.append(ch);
            }
        }
        return builder.toString();
    }

    private static String resolveMimeType(String filename) {
        String lower = filename.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".html")) return "text/html";
        if (lower.endsWith(".css")) return "text/css";
        if (lower.endsWith(".js")) return "application/javascript";
        if (lower.endsWith(".json")) return "application/json";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        return "application/octet-stream";
    }
}
