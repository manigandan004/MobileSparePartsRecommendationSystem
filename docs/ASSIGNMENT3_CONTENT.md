# Assignment 3 content reference

## Activity 1

### UserService
Purpose: Handles user registration and validation of user details.
Key behaviours: registration, duplicate-user prevention, password validation and user existence.
Reason: Core account-management business logic.

### AuthenticationService
Purpose: Handles login and credential verification.
Key behaviours: valid login, wrong password, unknown user and null credentials.
Reason: Core authentication business logic.

### SparePartService
Purpose: Manages spare-part stock and availability.
Key behaviours: add stock, get stock, availability checking and invalid quantity/model handling.
Reason: Core inventory business logic.

## Activity 2

JUnit 5 is configured through Maven using the `junit-jupiter` dependency. Test classes are under `src/test/java/com/msp/service` and follow the `<ClassName>Test.java` naming convention.

## Activity 3

UserServiceTest: valid registration, duplicate registration, short password, blank username.

AuthenticationServiceTest: valid credentials, wrong password, unknown user, null credentials.

SparePartServiceTest: stock addition, insufficient stock, negative required quantity, blank model.

## Activity 4

Final suite: 12 tests are expected to execute with 0 failures and 0 errors when the supplied project is run with Maven.

## Reflection

1. Unit testing verifies individual modules early, allowing defects to be isolated before integration and system testing.
2. The three modules were selected because they contain the project's main business logic for accounts, authentication and spare-part inventory.
3. The most useful edge-case tests are the invalid-input/exception tests because they verify how the system behaves at boundaries.
4. When a test fails, compare expected and actual results, inspect the test and implementation, correct the identified cause, and rerun the complete suite.
