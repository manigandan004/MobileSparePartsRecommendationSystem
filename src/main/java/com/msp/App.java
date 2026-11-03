
package com.msp;

<<<<<<< HEAD
import com.msp.model.Order;
=======
>>>>>>> origin/main
import com.msp.model.SparePart;
import com.msp.model.Review;
import com.msp.service.AuthenticationService;
import com.msp.service.ModelService;
import com.msp.service.OrderService;
<<<<<<< HEAD
import com.msp.service.RecommendationService;
import com.msp.service.RepairGuideService;
import com.msp.service.ReturnService;
import com.msp.service.PriceTrackingService;
import com.msp.service.ReviewService;
import com.msp.service.SparePartService;
import com.msp.service.UserService;

=======
import com.msp.service.PriceTrackingService;
import com.msp.service.RecommendationService;
import com.msp.service.RepairGuideService;
import com.msp.service.ReturnService;
import com.msp.service.SparePartService;
import com.msp.service.ReviewService;
import com.msp.service.UserService;
>>>>>>> origin/main
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
<<<<<<< HEAD
=======
import java.net.URI;
>>>>>>> origin/main
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
<<<<<<< HEAD
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class App {

    private static final int PORT = 3000;

    private static final UserService userService = new UserService();
    private static final AuthenticationService authenticationService =
            new AuthenticationService(userService);

    private static final SparePartService sparePartService =
            new SparePartService();

    private static final ModelService modelService = new ModelService();
    private static final OrderService orderService = new OrderService();

    private static final ReviewService reviewService = new ReviewService();
    private static final RecommendationService recommendationService =
            new RecommendationService(sparePartService);

    private static final RepairGuideService repairGuideService =
            new RepairGuideService();

    // MSPR-29: validate return requests against existing orders and parts.
    private static final ReturnService returnService =
            new ReturnService(orderService, sparePartService);

    private static final PriceTrackingService priceTrackingService =
            new PriceTrackingService();

    public static void main(String[] args) throws IOException {
        HttpServer server =
                HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/api/register", App::handleRegister);
        server.createContext("/api/login", App::handleLogin);
        server.createContext("/api/spare-parts", App::handleSpareParts);
        server.createContext("/api/models", App::handleModels);
        server.createContext("/api/brands", App::handleBrands);
        server.createContext("/api/product", App::handleProduct);
        server.createContext("/api/compatibility", App::handleCompatibility);
        server.createContext("/api/reviews", App::handleReviews);
        server.createContext("/api/orders", App::handleOrders);
        server.createContext("/api/compare", App::handleCompare);
        server.createContext("/api/recommendations",
                App::handleRecommendations);
        server.createContext("/api/repair-guides",
                App::handleRepairGuides);
        server.createContext("/api/returns", App::handleReturns);
        server.createContext("/api/price-tracking",
                App::handlePriceTracking);
        server.createContext("/api/notifications",
                App::handleNotifications);
        server.createContext("/", App::handleStatic);

        server.setExecutor(null);
        server.start();

        System.out.println(
                "Mobile Spare Parts Management System running at http://localhost:"
                        + PORT);
    }

    private static void handleRegister(HttpExchange e)
            throws IOException {
        if (!method(e, "POST")) return;

        Map<String, String> f = parseForm(e);

        try {
            if (userService.register(
                    f.get("username"),
                    f.get("password"),
                    f.get("email"))) {

                send(e, 201,
                        "{\"success\":true,\"message\":\"Registration successful\"}");
            } else {
                send(e, 409,
                        "{\"success\":false,\"message\":\"Username or email already exists\"}");
            }
        } catch (IllegalArgumentException x) {
            send(e, 400, jsonError(x.getMessage()));
        }
    }

    private static void handleLogin(HttpExchange e)
            throws IOException {
        if (!method(e, "POST")) return;

        Map<String, String> f = parseForm(e);

        boolean ok = authenticationService.authenticate(
                f.get("username"), f.get("password"));

        send(e, ok ? 200 : 401,
                ok
                        ? "{\"success\":true,\"message\":\"Login successful\"}"
                        : "{\"success\":false,\"message\":\"Invalid username or password\"}");
    }

    private static void handleSpareParts(HttpExchange e)
            throws IOException {
        if (!method(e, "GET")) return;

        Map<String, String> q = query(e);

        List<SparePart> list = sparePartService.search(
                q.get("search"), q.get("model"), q.get("category"));

        send(e, 200, partsJson(list));
    }

    private static void handleModels(HttpExchange e)
            throws IOException {
        if (!method(e, "GET")) return;

        send(e, 200,
                stringListJson(modelService.getModels(query(e).get("brand"))));
    }

    private static void handleBrands(HttpExchange e)
            throws IOException {
        if (!method(e, "GET")) return;

        send(e, 200, stringListJson(modelService.getBrands()));
    }

    private static void handleProduct(HttpExchange e)
            throws IOException {
        if (!method(e, "GET")) return;

        int id = intValue(query(e).get("id"), 0);
        SparePart p = sparePartService.findById(id);

        if (p == null) {
            send(e, 404, "{\"message\":\"Product not found\"}");
            return;
        }

        send(e, 200, partJson(p));
    }

    private static void handleCompatibility(HttpExchange e)
            throws IOException {
        if (!method(e, "GET")) return;

        Map<String, String> q = query(e);
        String model = q.get("model");

        send(e, 200,
                stringListJson(modelService.getModels(model)));
    }

    private static void handleReviews(HttpExchange e)
            throws IOException {
        if ("GET".equalsIgnoreCase(e.getRequestMethod())) {
            Map<String, String> q = query(e);
            int partId = intValue(q.get("partId"), 1);
            int minRating = intValue(q.get("minRating"), 1);
            List<Review> reviews = reviewService.getReviews(
                    partId, minRating, q.get("sort"));

            send(e, 200, "{\"averageRating\":"
                    + reviewService.averageRating(partId)
                    + ",\"reviews\":" + reviewsJson(reviews) + "}");
            return;
        }

        if ("POST".equalsIgnoreCase(e.getRequestMethod())) {
            Map<String, String> f = parseForm(e);

            try {
                Review r = reviewService.addReview(
                        intValue(f.get("partId"), 0),
                        f.get("username"),
                        intValue(f.get("rating"), 0),
                        f.get("comment"));

                send(e, 201, reviewJson(r));
            } catch (IllegalArgumentException x) {
                send(e, 400, jsonError(x.getMessage()));
            }
            return;
        }

        send(e, 405, "{\"message\":\"Method not allowed\"}");
    }

    private static void handleOrders(HttpExchange e)
            throws IOException {
        if ("GET".equalsIgnoreCase(e.getRequestMethod())) {
            Optional<Order> o =
                    orderService.find(intValue(query(e).get("id"), 0));

            if (o.isEmpty()) {
                send(e, 404, "{\"message\":\"Order not found\"}");
                return;
            }

            send(e, 200, orderJson(o.get()));
            return;
        }

        if ("POST".equalsIgnoreCase(e.getRequestMethod())) {
            Map<String, String> f = parseForm(e);

            try {
                Order o = orderService.placeOrder(
                        f.get("username"),
                        intValue(f.get("partId"), 0),
                        intValue(f.get("quantity"), 0),
                        doubleValue(f.get("unitPrice"), 0));

                send(e, 201, orderJson(o));
            } catch (IllegalArgumentException x) {
                send(e, 400, jsonError(x.getMessage()));
            }
            return;
        }

        if ("PUT".equalsIgnoreCase(e.getRequestMethod())) {
            Map<String, String> f = parseForm(e);

            try {
                boolean ok = orderService.updateStatus(
                        intValue(f.get("id"), 0),
                        f.get("status"));

                send(e, ok ? 200 : 404,
                        "{\"success\":" + ok + "}");
            } catch (IllegalArgumentException x) {
                send(e, 400, jsonError(x.getMessage()));
            }
            return;
        }

        send(e, 405, "{\"message\":\"Method not allowed\"}");
    }

    private static void handleCompare(HttpExchange e)
            throws IOException {
        if (!method(e, "GET")) return;

        String ids = query(e).getOrDefault("ids", "");
        List<SparePart> parts = new ArrayList<>();

        for (String s : ids.split(",")) {
            SparePart p = sparePartService.findById(intValue(s, 0));
            if (p != null) parts.add(p);
        }

        send(e, 200, partsJson(parts));
    }

    private static void handleRecommendations(HttpExchange e)
            throws IOException {
        if (!method(e, "GET")) return;

        Map<String, String> q = query(e);

        send(e, 200, partsJson(
                recommendationService.recommend(
                        q.get("model"), q.get("category"))));
    }

    private static void handleRepairGuides(HttpExchange e)
            throws IOException {
        if (!method(e, "GET")) return;

        send(e, 200,
                mapListJson(repairGuideService.guides(query(e).get("model"))));
    }

    // MSPR-29: defective-part return and replacement API.
    private static void handleReturns(HttpExchange e)
            throws IOException {

        if ("GET".equalsIgnoreCase(e.getRequestMethod())) {
            send(e, 200, mapListJson(returnService.all()));
            return;
        }

        if (!"POST".equalsIgnoreCase(e.getRequestMethod())) {
            send(e, 405, "{\"message\":\"Method not allowed\"}");
            return;
        }

        try {
            Map<String, String> f = parseForm(e);

            Map<String, String> created = returnService.create(
                    intValue(f.get("orderId"), 0),
                    intValue(f.get("partId"), 0),
                    f.get("requestType"),
                    f.get("reason"),
                    f.get("description"),
                    f.get("contact")
            );

            send(e, 201, mapJson(created));

        } catch (IllegalArgumentException x) {
            send(e, 400, jsonError(x.getMessage()));
        }
    }

    private static void handlePriceTracking(HttpExchange e)
            throws IOException {
        if (!method(e, "GET")) return;

        send(e, 200,
                mapObjectListJson(
                        priceTrackingService.prices(
                                sparePartService.getAllParts())));
    }

    private static void handleNotifications(HttpExchange e)
            throws IOException {
        if (!method(e, "GET")) return;

        List<Map<String, String>> notifications = new ArrayList<>();

        for (SparePart p : sparePartService.getAllParts()) {
            if (p.getQuantity() <= 8) {
                Map<String, String> notification = new HashMap<>();
                notification.put("type", "OUT_OF_STOCK_WARNING");
                notification.put("part", p.getPartName());
                notification.put("model", p.getModel());
                notification.put("message",
                        "Low stock: only " + p.getQuantity() + " left");

                notifications.add(notification);
            }
        }

        send(e, 200, mapListJson(notifications));
    }

    private static boolean method(HttpExchange e, String m)
            throws IOException {
        if (!m.equalsIgnoreCase(e.getRequestMethod())) {
            send(e, 405, "{\"message\":\"Method not allowed\"}");
            return false;
        }
        return true;
    }

    private static Map<String, String> parseForm(HttpExchange e)
            throws IOException {
        return parsePairs(new String(
                e.getRequestBody().readAllBytes(),
                StandardCharsets.UTF_8));
    }

    private static Map<String, String> query(HttpExchange e) {
        return parsePairs(e.getRequestURI().getRawQuery());
    }

    private static Map<String, String> parsePairs(String body) {
        Map<String, String> result = new HashMap<>();
        if (body == null) return result;

        for (String pair : body.split("&")) {
            if (pair.isBlank()) continue;

            String[] a = pair.split("=", 2);
            String key = url(a[0]);
            String value = a.length > 1 ? url(a[1]) : "";

            result.put(key, value);
        }

        return result;
    }

    private static String url(String s) {
        try {
            return URLDecoder.decode(s, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            return s;
        }
    }

    private static int intValue(String s, int d) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return d;
        }
    }

    private static double doubleValue(String s, double d) {
        try {
            return Double.parseDouble(s);
        } catch (Exception e) {
            return d;
        }
    }

    private static String partJson(SparePart p) {
        return "{\"id\":" + p.getId()
                + ",\"partName\":\"" + escape(p.getPartName())
                + "\",\"model\":\"" + escape(p.getModel())
                + "\",\"quantity\":" + p.getQuantity()
                + ",\"price\":" + String.format(Locale.US, "%.2f", p.getPrice())
                + "}";
    }

    private static String partsJson(List<SparePart> ps) {
        StringBuilder s = new StringBuilder("[");
        for (int i = 0; i < ps.size(); i++) {
            if (i > 0) s.append(',');
            s.append(partJson(ps.get(i)));
        }
        return s.append(']').toString();
    }

    private static String reviewsJson(List<Review> rs) {
        StringBuilder s = new StringBuilder("[");
        for (int i = 0; i < rs.size(); i++) {
            Review r = rs.get(i);
            if (i > 0) s.append(',');

            s.append("{\"id\":").append(r.getId())
                    .append(",\"partId\":").append(r.getPartId())
                    .append(",\"username\":\"").append(escape(r.getUsername()))
                    .append("\",\"rating\":").append(r.getRating())
                    .append(",\"comment\":\"").append(escape(r.getComment()))
                    .append("\"}");
        }
        return s.append(']').toString();
    }

    private static String reviewJson(Review r) {
        return "{\"id\":" + r.getId()
                + ",\"partId\":" + r.getPartId()
                + ",\"username\":\"" + escape(r.getUsername())
                + "\",\"rating\":" + r.getRating()
                + ",\"comment\":\"" + escape(r.getComment()) + "\"}";
    }

    private static String orderJson(Order o) {
        return "{\"id\":" + o.getId()
                + ",\"username\":\"" + escape(o.getUsername())
                + "\",\"partId\":" + o.getPartId()
                + ",\"quantity\":" + o.getQuantity()
                + ",\"total\":" + String.format(Locale.US, "%.2f", o.getTotal())
                + ",\"status\":\"" + escape(o.getStatus()) + "\"}";
    }

    private static String stringListJson(List<String> list) {
        StringBuilder s = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) s.append(',');
            s.append("\"").append(escape(list.get(i))).append("\"");
        }
        return s.append(']').toString();
    }

    private static String mapJson(Map<String, String> m) {
        StringBuilder s = new StringBuilder("{");
        int i = 0;

        for (var entry : m.entrySet()) {
            if (i++ > 0) s.append(',');
            s.append("\"").append(escape(entry.getKey()))
                    .append("\":\"").append(escape(entry.getValue()))
                    .append("\"");
        }

        return s.append('}').toString();
    }

    private static String mapListJson(List<Map<String, String>> list) {
        StringBuilder s = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) s.append(',');
            s.append(mapJson(list.get(i)));
        }
        return s.append(']').toString();
    }

    private static String mapObjectListJson(List<Map<String, Object>> list) {
        StringBuilder s = new StringBuilder("[");

        for (int i = 0; i < list.size(); i++) {
            if (i > 0) s.append(',');

            Map<String, Object> m = list.get(i);
            s.append('{');
            int j = 0;

            for (var entry : m.entrySet()) {
                if (j++ > 0) s.append(',');

                s.append("\"").append(escape(entry.getKey())).append("\":");
                Object value = entry.getValue();

                if (value instanceof Number || value instanceof Boolean) {
                    s.append(value);
                } else {
                    s.append("\"").append(escape(String.valueOf(value)))
                            .append("\"");
                }
            }

            s.append('}');
        }

        return s.append(']').toString();
    }

    private static String jsonError(String message) {
        return "{\"success\":false,\"message\":\"" + escape(message) + "\"}";
    }

    private static String escape(String v) {
        if (v == null) return "";
        return v.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", " ")
                .replace("\r", " ")
                .replace("\t", " ");
    }

    private static void send(HttpExchange e, int status, String body)
            throws IOException {
        byte[] data = body.getBytes(StandardCharsets.UTF_8);

        e.getResponseHeaders().set(
                "Content-Type", "application/json; charset=UTF-8");

        e.sendResponseHeaders(status, data.length);

        try (OutputStream out = e.getResponseBody()) {
            out.write(data);
        }
    }

    private static void handleStatic(HttpExchange e)
            throws IOException {
        String p = e.getRequestURI().getPath();

        if ("/".equals(p)) p = "/index.html";

        if (p.contains("..")) {
            send(e, 400, "Invalid path");
            return;
        }

        Path f = Path.of("frontend", p.substring(1));

        if (!Files.exists(f) || Files.isDirectory(f)) {
            send(e, 404, "Page not found");
            return;
        }

        byte[] data = Files.readAllBytes(f);
        e.getResponseHeaders().set("Content-Type", contentType(f));
        e.sendResponseHeaders(200, data.length);

        try (OutputStream out = e.getResponseBody()) {
            out.write(data);
        }
    }

    private static String contentType(Path f) {
        String name = f.getFileName().toString().toLowerCase(Locale.ROOT);

        if (name.endsWith(".html")) return "text/html; charset=UTF-8";
        if (name.endsWith(".css")) return "text/css; charset=UTF-8";
        if (name.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (name.endsWith(".json")) return "application/json; charset=UTF-8";
        if (name.endsWith(".png")) return "image/png";
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return "image/jpeg";
        if (name.endsWith(".svg")) return "image/svg+xml";

=======
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
>>>>>>> origin/main
        return "application/octet-stream";
    }
}
