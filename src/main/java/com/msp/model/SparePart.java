package com.msp.model;

import java.util.Objects;

public final class SparePart {
    private final int id;
    private final String partName;
    private final String model;
    private int quantity;
    private final double price;

    public SparePart(int id, String partName, String model, int quantity, double price) {
        if (id <= 0) throw new IllegalArgumentException("ID must be positive");
        if (partName == null || partName.isBlank()) throw new IllegalArgumentException("Part name is required");
        if (model == null || model.isBlank()) throw new IllegalArgumentException("Model is required");
        if (quantity < 0) throw new IllegalArgumentException("Quantity cannot be negative");
        if (price < 0) throw new IllegalArgumentException("Price cannot be negative");
        this.id = id;
        this.partName = partName.trim();
        this.model = model.trim();
        this.quantity = quantity;
        this.price = price;
    }

    public int getId() { return id; }
    public String getPartName() { return partName; }
    public String getModel() { return model; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }

    public void addQuantity(int amount) {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");
        quantity += amount;
    }

    @Override
    public String toString() {
        return partName + " (" + model + ") - " + quantity;
    }
}
