package com.msp.service;
import org.junit.jupiter.api.Test; import static org.junit.jupiter.api.Assertions.*;
class ReturnServiceTest { @Test void createsReturnRequest(){ReturnService s=new ReturnService();var r=s.create(1001,"Defective part");assertEquals("REQUESTED",r.get("status"));} @Test void rejectsMissingReason(){assertThrows(IllegalArgumentException.class,()->new ReturnService().create(1,""));} }
