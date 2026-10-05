package com.msp.service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
public class ReturnService {
    private final AtomicInteger ids=new AtomicInteger(1); private final List<Map<String,String>> requests=new ArrayList<>();
    public synchronized Map<String,String> create(int orderId,String reason){
        if(orderId<=0||reason==null||reason.isBlank()) throw new IllegalArgumentException("Order ID and reason are required");
        Map<String,String> r=new LinkedHashMap<>(); r.put("id",String.valueOf(ids.getAndIncrement())); r.put("orderId",String.valueOf(orderId)); r.put("reason",reason.trim()); r.put("status","REQUESTED"); requests.add(r); return r;
    }
    public synchronized List<Map<String,String>> all(){return new ArrayList<>(requests);}
}
