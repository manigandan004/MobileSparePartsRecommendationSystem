package com.msp.service;

import com.msp.model.SparePart;
import java.util.*;
import java.util.stream.Collectors;

public class RecommendationService {
    private final SparePartService parts;
    public RecommendationService(SparePartService parts){this.parts=parts;}
    public List<SparePart> recommend(String model, String category){
        String m=model==null?"":model.trim().toLowerCase(); String c=category==null?"":category.trim().toLowerCase();
        return parts.getAllParts().stream().filter(p -> (m.isBlank() || p.getModel().toLowerCase().contains(m)) && (c.isBlank() || p.getPartName().toLowerCase().contains(c)))
                .sorted(Comparator.comparingInt(SparePart::getQuantity).reversed().thenComparingDouble(SparePart::getPrice)).collect(Collectors.toList());
    }
}
