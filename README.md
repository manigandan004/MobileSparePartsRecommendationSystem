# Mobile Spare Parts Management System

Team 01 project. Sprint 1 foundation + Sprint 2 completed work implementation.

## Sprint 2

MSPR-3 User Registration; MSPR-4 User Login; MSPR-5 Browse Mobile Spare Parts; MSPR-6 Select Mobile Brand and Model; MSPR-7 Search Spare Parts; MSPR-9 View Product Details; MSPR-10 Check Spare-Part Compatibility; MSPR-26 Part Out of Stock Notifications; MSPR-27 Integrated Repair Guides; MSPR-29 Defective Part Return & Replacement; MSPR-20 Price Tracking.

Supporting features include product reviews/ratings, order tracking, comparison and recommendations.

## Run

Requires JDK 17+ and Maven.

```bash
mvn clean test
mvn clean compile
java -cp target/classes com.msp.App
```

Open `http://localhost:8080/`.

The current implementation uses an in-memory service layer for the web demo. `database/schema.sql` remains the database schema reference.
