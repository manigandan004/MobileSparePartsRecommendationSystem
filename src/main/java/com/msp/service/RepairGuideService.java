package com.msp.service;

import java.util.*;
public class RepairGuideService {
    public List<Map<String,String>> guides(String model){
        String m=model==null||model.isBlank()?"your mobile":model;
        return List.of(
            Map.of("title","Battery replacement","model",m,"steps","Power off; remove back cover; disconnect battery; replace battery; test charging."),
            Map.of("title","Charging port cleaning","model",m,"steps","Power off; inspect port; clean gently; test cable and charging."),
            Map.of("title","Display replacement","model",m,"steps","Power off; remove display assembly; disconnect flex cables; fit new display; test touch."));
    }
}
