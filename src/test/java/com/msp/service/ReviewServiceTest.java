package com.msp.service;
import org.junit.jupiter.api.Test; import static org.junit.jupiter.api.Assertions.*;
class ReviewServiceTest {
 @Test void addsAndAverages(){ReviewService s=new ReviewService();s.addReview(1,"Manoj",5,"Good");s.addReview(1,"Alex",3,"Okay");assertEquals(4.0,s.averageRating(1));}
 @Test void rejectsInvalidRating(){assertThrows(IllegalArgumentException.class,()->new ReviewService().addReview(1,"u",6,"x"));}
}
