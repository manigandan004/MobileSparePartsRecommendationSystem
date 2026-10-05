package com.msp.service;
import org.junit.jupiter.api.Test; import static org.junit.jupiter.api.Assertions.*;
class ModelServiceTest { @Test void returnsBrandsAndModels(){ModelService s=new ModelService();assertTrue(s.getBrands().contains("Apple"));assertTrue(s.getModels("Apple").contains("iPhone 15"));} }
