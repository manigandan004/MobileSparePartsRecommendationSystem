
package com.msp;

import com.msp.model.Order;
import com.msp.model.SparePart;
import com.msp.model.Review;
import com.msp.service.AuthenticationService;
import com.msp.service.ModelService;
import com.msp.service.OrderService;
import com.msp.service.RecommendationService;
import com.msp.service.RepairGuideService;
import com.msp.service.ReturnService;
import com.msp.service.PriceTrackingService;
import com.msp.service.ReviewService;
import com.msp.service.SparePartService;
import com.msp.service.UserService;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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

        return "application/octet-stream";
    }
}
