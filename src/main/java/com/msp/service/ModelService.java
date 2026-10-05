package com.msp.service;

import java.util.*;

public class ModelService {
    private final Map<String,List<String>> models = new LinkedHashMap<>();
    public ModelService(){
        models.put("Apple", List.of("iPhone 15","iPhone 14","iPhone 13"));
        models.put("Samsung", List.of("Samsung S24","Samsung S23","Galaxy A55"));
        models.put("OnePlus", List.of("OnePlus 12","OnePlus 11","Nord CE 4"));
    }
    public List<String> getBrands(){return new ArrayList<>(models.keySet());}
    public List<String> getModels(String brand){ if(brand==null||brand.isBlank()) return models.values().stream().flatMap(Collection::stream).distinct().toList(); return models.getOrDefault(brand, List.of()); }
}
