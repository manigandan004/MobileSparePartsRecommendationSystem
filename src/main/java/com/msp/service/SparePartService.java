package com.msp.service;

import com.msp.model.SparePart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SparePartService {
    private final List<SparePart> parts = new ArrayList<>();
    private int nextId = 1;

    public SparePartService() {
        this(new ModelService());
    }

    public SparePartService(ModelService modelService) {
        addSeedPart("Display Assembly", "iPhone 15", 10, 8500.00);
        addSeedPart("Battery", "Samsung S24", 15, 4200.00);
        addSeedPart("Charging Port", "OnePlus 12", 12, 1800.00);
        addSeedPart("Back Glass", "iPhone 14", 8, 3500.00);

        for (Map<String, String> device : modelService.getModelCatalog()) {
            String model = device.get("name");
            addSeedPartIfMissing("Battery", model, 8, 1800.00);
            addSeedPartIfMissing("Display Assembly", model, 6, 5200.00);
            addSeedPartIfMissing("Charging Port", model, 8, 1400.00);
            addSeedPartIfMissing("Rear Camera", model, 5, 3200.00);
        }
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

        parts.add(new SparePart(nextId++, "General Spare Part", model.trim(), quantity, 0.0));
    }

    public synchronized boolean isAvailable(String model, int requiredQuantity) {
        if (requiredQuantity < 0) {
            throw new IllegalArgumentException("Required quantity cannot be negative");
        }
        if (model == null || model.isBlank()) return false;

        String normalized = model.trim().toLowerCase(Locale.ROOT);
        return parts.stream()
                .filter(p -> p.getModel().toLowerCase(Locale.ROOT).equals(normalized))
                .mapToInt(SparePart::getQuantity)
                .sum() >= requiredQuantity;
    }

    public synchronized int getStock(String model) {
        if (model == null || model.isBlank()) return 0;
        String normalized = model.trim().toLowerCase(Locale.ROOT);
        return parts.stream()
                .filter(p -> p.getModel().toLowerCase(Locale.ROOT).equals(normalized))
                .mapToInt(SparePart::getQuantity)
                .sum();
    }

    public synchronized List<SparePart> getAllParts() {
        return Collections.unmodifiableList(new ArrayList<>(parts));
    }

    public synchronized SparePart findById(int id) {
        return parts.stream().filter(p -> p.getId() == id).findFirst().orElse(null);
    }

    public synchronized List<SparePart> search(String query, String model, String category) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        String m = model == null ? "" : model.trim().toLowerCase(Locale.ROOT);
        String c = category == null ? "" : category.trim().toLowerCase(Locale.ROOT);
        return parts.stream().filter(p ->
                (q.isBlank() || p.getPartName().toLowerCase(Locale.ROOT).contains(q) || p.getModel().toLowerCase(Locale.ROOT).contains(q))
                && (m.isBlank() || p.getModel().toLowerCase(Locale.ROOT).contains(m))
                && (c.isBlank() || p.getPartName().toLowerCase(Locale.ROOT).contains(c))
        ).toList();
    }

    private void addSeedPartIfMissing(String partName, String model, int quantity, double price) {
        boolean exists = parts.stream().anyMatch(part ->
                part.getModel().equalsIgnoreCase(model)
                        && part.getPartName().equalsIgnoreCase(partName));
        if (!exists) {
            addSeedPart(partName, model, quantity, price);
        }
    }

    private void addSeedPart(String partName, String model, int quantity, double price) {
        parts.add(new SparePart(nextId++, partName, model, quantity, price));
    }
}
