# Book Store Automation Tests

This project contains automated tests for the Book Store application using a hybrid approach combining UI and API testing.

## Tech Stack

- **Java 21** - Programming language
- **Maven** - Build tool and dependency management
- **Selenide 7.5.1** - UI automation framework (wrapper around Selenium WebDriver)
- **TestNG 7.10.2** - Test framework
- **Rest Assured 5.5.0** - API testing library
- **Allure 2.29.0** - Test reporting framework
- **AssertJ 3.26.3** - Assertion library
- **Lombok 1.18.34** - Code generation for boilerplate reduction
- **Jackson 2.17.2** - JSON processing

## Project Structure

```
src/
├── main/
│   └── java/
│       ├── api/
│       │   ├── client/        # API client for user management
│       │   └── model/         # Request/Response DTOs
│       ├── config/            # Configuration management
│       └── ui/                # Page Object Model
│           ├── BasePage.java
│           ├── LoginPage.java
│           └── BookStorePage.java
└── test/
    └── java/
        ├── listener/          # TestNG listeners (Allure attachments)
        └── tests/            # Test classes
            ├── BaseTest.java
            └── AuthorizationTest.java
```

## Test Coverage

### Authorization Tests

- **Authorized user sees username and profile icon** - Verifies that after successful login, the user sees their profile icon and username displayed
- **Unauthorized user sees Login button** - Verifies that non-authenticated users see the Login button instead of profile
- **Unauthorized user does not see collection checkboxes** - Verifies that book collection checkboxes are hidden for unauthorized users

## Configuration

The project requires a `config.properties` file in `src/test/resources/` with the following properties:

```properties
ui.base.url=https://demoqa.com
ui.path.books=/books
ui.path.login=/login
ui.path.profile=/profile
api.base.url=https://demoqa.com
```

Properties can also be overridden via system properties using `-D` flag.

## Running Tests

### Run all tests:
```bash
mvn clean test
```

### Run specific test class:
```bash
mvn test -Dtest=AuthorizationTest
```

### Run specific test method:
```bash
mvn test -Dtest=AuthorizationTest#authorizedUserSeesUsernameAndProfileIcon
```

## Allure Reports

After running tests, generate and view the Allure report:

```bash
mvn allure:serve
```

Or generate a static report:
```bash
allure generate allure-results --clean
allure open
```

## Test Approach

This project follows a hybrid testing approach:

1. **API for test data setup** - Test users are created via API to ensure clean state and faster execution
2. **UI for actual test execution** - Login and UI interactions are performed through the browser to test the actual user flow
3. **API for cleanup** - Test data is cleaned up via API after each test

This approach ensures:
- Faster test execution (API is faster than UI for setup/teardown)
- Isolated test scenarios
- Realistic UI testing for the actual user journey
- Clean test environment after each run