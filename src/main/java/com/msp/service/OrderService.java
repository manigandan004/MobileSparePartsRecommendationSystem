package com.msp.service;

import com.msp.model.Order;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class OrderService {
    private final List<Order> orders = new ArrayList<>();
    private final AtomicInteger ids = new AtomicInteger(1001);

    public synchronized Order placeOrder(String username, int partId, int quantity, double unitPrice) {
        if (username == null || username.isBlank()) throw new IllegalArgumentException("Username is required");
        if (partId <= 0 || quantity <= 0 || unitPrice < 0) throw new IllegalArgumentException("Invalid order details");
        Order o = new Order(ids.getAndIncrement(), username.trim(), partId, quantity, quantity*unitPrice);
        orders.add(o); return o;
    }
    public synchronized Optional<Order> find(int id) { return orders.stream().filter(o->o.getId()==id).findFirst(); }
    public synchronized boolean updateStatus(int id, String status) {
        Optional<Order> o=find(id); if(o.isEmpty()) return false;
        List<String> valid=List.of("PLACED","PACKED","SHIPPED","OUT_FOR_DELIVERY","DELIVERED","CANCELLED");
        if(!valid.contains(status)) throw new IllegalArgumentException("Invalid order status");
        o.get().setStatus(status); return true;
    }
    public synchronized List<Order> all(){return new ArrayList<>(orders);}
}
