package com.msp.service;

import com.msp.model.Review;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ReviewService {
    private final List<Review> reviews = new ArrayList<>();
    private final AtomicInteger ids = new AtomicInteger(1);

    public synchronized Review addReview(int partId, String username, int rating, String comment) {
        if (partId <= 0) throw new IllegalArgumentException("Valid product is required");
        if (username == null || username.isBlank()) throw new IllegalArgumentException("Username is required");
        if (rating < 1 || rating > 5) throw new IllegalArgumentException("Rating must be between 1 and 5");
        if (comment == null || comment.isBlank()) throw new IllegalArgumentException("Comment is required");
        Review r = new Review(ids.getAndIncrement(), partId, username.trim(), rating, comment.trim());
        reviews.add(r); return r;
    }
    public synchronized List<Review> getReviews(int partId, int minRating, String sort) {
        return reviews.stream().filter(r -> r.getPartId()==partId && r.getRating()>=minRating)
                .sorted((a,b) -> "rating".equalsIgnoreCase(sort) ? Integer.compare(b.getRating(), a.getRating()) : b.getDate().compareTo(a.getDate()))
                .collect(Collectors.toList());
    }
    public synchronized double averageRating(int partId) { return reviews.stream().filter(r->r.getPartId()==partId).mapToInt(Review::getRating).average().orElse(0); }
}
