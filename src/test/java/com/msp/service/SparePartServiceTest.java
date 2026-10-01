package com.msp.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SparePartServiceTest {

    @Test
    void stockAddedAndAvailable() {
        SparePartService service = new SparePartService();

        int before = service.getStock("iPhone 15");
        service.addPart("iPhone 15", 5);

        assertEquals(before + 5, service.getStock("iPhone 15"));
        assertTrue(service.isAvailable("iPhone 15", before + 5));
    }

    @Test
    void insufficientStockRejected() {
        SparePartService service = new SparePartService();

        assertFalse(service.isAvailable("iPhone 15", 1000));
    }

    @Test
    void negativeRequiredQuantityThrows() {
        SparePartService service = new SparePartService();

        assertThrows(IllegalArgumentException.class,
                () -> service.isAvailable("iPhone 15", -1));
    }

    @Test
    void blankModelThrowsWhenAdding() {
        SparePartService service = new SparePartService();

        assertThrows(IllegalArgumentException.class,
                () -> service.addPart("", 1));
    }
}
