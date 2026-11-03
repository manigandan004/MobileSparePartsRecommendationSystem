
package com.msp.service;

import com.msp.model.Order;
import com.msp.model.SparePart;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class ReturnService {
    private final AtomicInteger ids = new AtomicInteger(1);
    private final List<Map<String, String>> requests = new ArrayList<>();

    private final OrderService orderService;
    private final SparePartService sparePartService;

    // Retains compatibility with the existing ReturnServiceTest.
    public ReturnService() {
        this(null, null);
    }

    // Used by the application to validate real orders and spare parts.
    public ReturnService(
            OrderService orderService,
            SparePartService sparePartService) {
        this.orderService = orderService;
        this.sparePartService = sparePartService;
    }

    // Backward-compatible method for existing callers and tests.
    public synchronized Map<String, String> create(
            int orderId, String reason) {

        validateBasicDetails(orderId, reason);

        Map<String, String> request = new LinkedHashMap<>();
        request.put("id", String.valueOf(ids.getAndIncrement()));
        request.put("orderId", String.valueOf(orderId));
        request.put("reason", reason.trim());
        request.put("status", "REQUESTED");
        request.put("createdAt", LocalDateTime.now().toString());

        requests.add(request);
        return new LinkedHashMap<>(request);
    }

    // Full MSPR-29 return/replacement request.
    public synchronized Map<String, String> create(
            int orderId,
            int partId,
            String requestType,
            String reason,
            String description,
            String contact) {

        validateBasicDetails(orderId, reason);

        if (partId <= 0) {
            throw new IllegalArgumentException(
                    "A valid spare-part ID is required");
        }

        if (requestType == null ||
                !(requestType.equals("RETURN")
                || requestType.equals("REPLACEMENT"))) {
            throw new IllegalArgumentException(
                    "Request type must be RETURN or REPLACEMENT");
        }

        if (description == null || description.trim().length() < 10) {
            throw new IllegalArgumentException(
                    "Description must contain at least 10 characters");
        }

        if (description.trim().length() > 1000) {
            throw new IllegalArgumentException(
                    "Description cannot exceed 1000 characters");
        }

        if (orderService == null || sparePartService == null) {
            throw new IllegalStateException(
                    "Order and spare-part validation is not configured");
        }

        Optional<Order> order = orderService.find(orderId);
        if (order.isEmpty()) {
            throw new IllegalArgumentException("Order not found");
        }

        SparePart part = sparePartService.findById(partId);
        if (part == null) {
            throw new IllegalArgumentException("Spare part not found");
        }

        if (order.get().getPartId() != partId) {
            throw new IllegalArgumentException(
                    "The selected spare part does not belong to this order");
        }

        String normalizedContact =
                contact == null ? "" : contact.trim();

        if (!normalizedContact.isEmpty()
                && !normalizedContact.matches(
                "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException(
                    "Enter a valid contact email");
        }

        Map<String, String> request = new LinkedHashMap<>();
        request.put("id", String.valueOf(ids.getAndIncrement()));
        request.put("orderId", String.valueOf(orderId));
        request.put("partId", String.valueOf(partId));
        request.put("requestType", requestType);
        request.put("reason", reason.trim());
        request.put("description", description.trim());
        request.put("contact", normalizedContact);
        request.put("status", "REQUESTED");
        request.put("createdAt", LocalDateTime.now().toString());

        requests.add(request);
        return new LinkedHashMap<>(request);
    }

    private void validateBasicDetails(int orderId, String reason) {
        if (orderId <= 0 || reason == null || reason.isBlank()) {
            throw new IllegalArgumentException(
                    "Order ID and reason are required");
        }
    }

    public synchronized List<Map<String, String>> all() {
        List<Map<String, String>> result = new ArrayList<>();

        for (Map<String, String> request : requests) {
            result.add(new LinkedHashMap<>(request));
        }

        return result;
    }
}
