# Mobile Spare Parts Management System

A Java 17 Maven mini-project for managing mobile spare parts, user registration, authentication and model-based spare-part lookup.

## Jira feature mapping

| Jira Feature | Implemented area |
|---|---|
| MSPR-21 Create DB schema for user accounts | `database/schema.sql` |
| MSPR-22 Develop UI for registration page | `frontend/register.html` |
| MSPR-23 Implement authentication logic | `UserService` + `AuthenticationService` |
| MSPR-24 Create API for fetching spare parts | `GET /api/spare-parts` |
| MSPR-25 Build Dropdown UI for models | `frontend/models.html` |

## Requirements

- JDK 17 or newer
- Maven 3.9+ for tests
- Optional: MySQL 8+ if you want to execute `database/schema.sql`

## Run the application

From the project root:

```bash
mvn compile
java -cp target/classes com.msp.App
```

Then open:

- http://localhost:8080/
- http://localhost:8080/register.html
- http://localhost:8080/models.html
- http://localhost:8080/api/spare-parts
- http://localhost:8080/api/models

## Run unit tests

```bash
mvn clean test
```

The project contains 12 JUnit 5 tests across the three core service modules.

## Generate JaCoCo coverage

```bash
mvn clean test jacoco:report
```

Open:

```text
target/site/jacoco/index.html
```

## Git

```bash
git init
git add .
git commit -m "Initial Mobile Spare Parts Management System"
git branch -M main
git remote add origin YOUR_GITHUB_REPOSITORY_URL
git push -u origin main
```

After adding tests/changes:

```bash
git add .
git commit -m "Add JUnit 5 tests and coverage"
git push
```

## Important

The SQL schema is provided for the database requirement. The Java demo application deliberately uses an in-memory service layer so that the project can be demonstrated without requiring a local database server. This keeps the application easy to run while preserving the MySQL schema needed for the database task.

## Sprint 2 progress
- MSPR-3 User Registration: implemented and tested first.
- Remaining Sprint 2 work items: not implemented in this step.
