package com.msp.model;

import java.time.LocalDateTime;

public class Order {
    private final int id;
    private final String username;
    private final int partId;
    private final int quantity;
    private final double total;
    private String status;
    private final LocalDateTime createdAt;

    public Order(int id, String username, int partId, int quantity, double total) {
        this.id=id; this.username=username; this.partId=partId; this.quantity=quantity; this.total=total;
        this.status="PLACED"; this.createdAt=LocalDateTime.now();
    }
    public int getId(){return id;} public String getUsername(){return username;} public int getPartId(){return partId;}
    public int getQuantity(){return quantity;} public double getTotal(){return total;} public String getStatus(){return status;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setStatus(String status){this.status=status;}
}
