package com.msp.model;

import java.time.LocalDateTime;

public class Review {
    private final int id;
    private final int partId;
    private final String username;
    private final int rating;
    private final String comment;
    private final LocalDateTime date;

    public Review(int id, int partId, String username, int rating, String comment) {
        this.id = id; this.partId = partId; this.username = username; this.rating = rating;
        this.comment = comment; this.date = LocalDateTime.now();
    }
    public int getId(){return id;} public int getPartId(){return partId;} public String getUsername(){return username;}
    public int getRating(){return rating;} public String getComment(){return comment;} public LocalDateTime getDate(){return date;}
}
