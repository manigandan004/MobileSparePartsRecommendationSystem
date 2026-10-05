package com.msp.service;
import org.junit.jupiter.api.Test; import static org.junit.jupiter.api.Assertions.*;
class RecommendationServiceTest { @Test void filtersByModel(){SparePartService p=new SparePartService();var r=new RecommendationService(p).recommend("iPhone 15","");assertFalse(r.isEmpty());assertEquals("iPhone 15",r.get(0).getModel());} }
