package com.msp.service;

import com.msp.model.SparePart;
import java.util.*;
public class PriceTrackingService {
    public List<Map<String,Object>> prices(List<SparePart> parts){
        List<Map<String,Object>> out=new ArrayList<>();
        for(SparePart p:parts){Map<String,Object> m=new LinkedHashMap<>(); m.put("id",p.getId()); m.put("partName",p.getPartName()); m.put("model",p.getModel()); m.put("currentPrice",p.getPrice()); m.put("previousPrice",Math.round(p.getPrice()*1.05*100.0)/100.0); m.put("change",-5); out.add(m);} return out;
    }
}
