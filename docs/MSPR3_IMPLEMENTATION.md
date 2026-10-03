# MSPR-3 - User Registration

Implemented first from the Sprint 2 backlog.

## Implemented
- Registration form with username, email, password and confirm-password fields.
- Client-side password confirmation.
- Server-side validation for username, password and email.
- Duplicate username and duplicate email prevention.
- Registration API: `POST /api/register`.
- HTTP responses for success, duplicate account and validation errors.
- JUnit 5 tests covering normal, duplicate and invalid registration cases.

## Run
```bash
mvn clean test
mvn clean compile
java -cp target/classes com.msp.App
```

Open `http://localhost:8080/register.html`.

Only MSPR-3 is implemented in this step. The remaining Sprint 2 stories should be added one at a time after MSPR-3 is verified.
