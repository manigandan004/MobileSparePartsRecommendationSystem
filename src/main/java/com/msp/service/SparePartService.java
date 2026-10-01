package com.msp.service;

import com.msp.model.SparePart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class SparePartService {
    private final List<SparePart> parts = new ArrayList<>();

    public SparePartService() {
        parts.add(new SparePart(1, "Display Assembly", "iPhone 15", 10, 8500.00));
        parts.add(new SparePart(2, "Battery", "Samsung S24", 15, 4200.00));
        parts.add(new SparePart(3, "Charging Port", "OnePlus 12", 12, 1800.00));
        parts.add(new SparePart(4, "Back Glass", "iPhone 14", 8, 3500.00));
    }

    public synchronized void addPart(String model, int quantity) {
        if (model == null || model.isBlank()) throw new IllegalArgumentException("Model is required");
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");

        String normalized = model.trim().toLowerCase(Locale.ROOT);
        for (SparePart part : parts) {
            if (part.getModel().toLowerCase(Locale.ROOT).equals(normalized)) {
                part.addQuantity(quantity);
                return;
            }
        }

        int nextId = parts.stream().mapToInt(SparePart::getId).max().orElse(0) + 1;
        parts.add(new SparePart(nextId, "General Spare Part", model.trim(), quantity, 0.0));
    }

    public synchronized boolean isAvailable(String model, int requiredQuantity) {
        if (requiredQuantity < 0) {
            throw new IllegalArgumentException("Required quantity cannot be negative");
        }
        if (model == null || model.isBlank()) return false;

        String normalized = model.trim().toLowerCase(Locale.ROOT);
        return parts.stream()
                .filter(p -> p.getModel().toLowerCase(Locale.ROOT).equals(normalized))
                .findFirst()
                .map(p -> p.getQuantity() >= requiredQuantity)
                .orElse(false);
    }

    public synchronized int getStock(String model) {
        if (model == null || model.isBlank()) return 0;
        String normalized = model.trim().toLowerCase(Locale.ROOT);
        return parts.stream()
                .filter(p -> p.getModel().toLowerCase(Locale.ROOT).equals(normalized))
                .findFirst()
                .map(SparePart::getQuantity)
                .orElse(0);
    }

    public synchronized List<SparePart> getAllParts() {
        return Collections.unmodifiableList(new ArrayList<>(parts));
    }
}
