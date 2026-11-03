
package com.msp.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Collection;

public class ModelService {

    private final Map<String, List<String>> models = new LinkedHashMap<>();

    public ModelService() {
        models.put("Apple", List.of(
                "iPhone 15", "iPhone 14", "iPhone 13"));

        models.put("Samsung", List.of(
                "Samsung S24", "Samsung S23", "Galaxy A55"));

        models.put("OnePlus", List.of(
                "OnePlus 12", "OnePlus 11", "Nord CE 4"));
    }

    public List<String> getBrands() {
        return new ArrayList<>(models.keySet());
    }

    public List<String> getModels(String brand) {
        if (brand == null || brand.isBlank()) {
            return models.values()
                    .stream()
                    .flatMap(Collection::stream)
                    .distinct()
                    .toList();
        }

        return models.getOrDefault(brand, List.of());
    }

    public List<Map<String, String>> getModelCatalog() {
        List<Map<String, String>> catalog = new ArrayList<>();

        for (Map.Entry<String, List<String>> entry : models.entrySet()) {
            for (String model : entry.getValue()) {
                Map<String, String> device = new LinkedHashMap<>();
                device.put("brand", entry.getKey());
                device.put("model", model);
                catalog.add(device);
            }
        }

        return catalog;
    }

    public String getBrandForModel(String model) {
        if (model == null || model.isBlank()) {
            return "";
        }

        for (Map.Entry<String, List<String>> entry : models.entrySet()) {
            if (entry.getValue().contains(model)) {
                return entry.getKey();
            }
        }

        return "";
    }
}
