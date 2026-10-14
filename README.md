## MSPR-20: Price Tracking

Implemented a price-tracking page for the Mobile Spare Parts Management System.

- **Frontend:** `frontend/price-tracking.html`
- **API endpoints:**
  - `GET /api/price-tracking`
  - `POST /api/price-tracking`
  - `DELETE /api/price-tracking?id=...`
- **Service:** `src/main/java/com/msp/service/PriceTrackingService.java`

The current implementation generates price comparison data and supports creating and deleting target-price alerts. Watchlist entries are stored in server memory and are not persisted across server restarts.