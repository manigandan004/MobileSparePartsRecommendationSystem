package com.msp.service;
import org.junit.jupiter.api.Test; import static org.junit.jupiter.api.Assertions.*;
class OrderServiceTest {
 @Test void placesOrder(){OrderService s=new OrderService();var o=s.placeOrder("u",1,2,100);assertEquals(200,o.getTotal());assertEquals("PLACED",o.getStatus());}
 @Test void updatesStatus(){OrderService s=new OrderService();var o=s.placeOrder("u",1,1,100);assertTrue(s.updateStatus(o.getId(),"SHIPPED"));assertEquals("SHIPPED",s.find(o.getId()).get().getStatus());}
}
