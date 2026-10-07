# Book Store Automation Tests
[![Run Automation Tests](https://github.com/Valeriya-hub/java-rest-assured-framework/actions/workflows/tests.yml/badge.svg?branch=main)](https://github.com/Valeriya-hub/java-rest-assured-framework/actions/workflows/tests.yml)
![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)
![Selenide](https://img.shields.io/badge/Selenide-7.5.1-blue)
![RestAssured](https://img.shields.io/badge/REST--Assured-5.5.0-green)
![Allure](https://img.shields.io/badge/Allure-2.29.0-red?logo=qameta)

This project contains automated tests for the Book Store application using a hybrid approach combining UI and API testing.

## 📊 Test Report
Latest Allure report: [https://valeriya-hub.github.io/java-rest-assured-framework/](https://valeriya-hub.github.io/java-rest-assured-framework/)

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

## Test Groups

Tests are organized into TestNG groups so they can be run selectively,
without executing the full suite every time. 

Groups let CI run smoke on every push for fast feedback, while regression runs nightly or before a release.

| Group        | Description                                           |
|--------------|-------------------------------------------------------|
| `smoke`      | Fast, critical-path checks                            |
| `regression` | Full coverage, including negative scenarios           |
| `ui`         | Tests that interact with the browser via Selenide     |
| `api`        | Pure API tests, no browser involved                   |
| `e2e`        | Cross-layer flows: UI action verified through the API |

### Running tests by group

```bash
# Run only smoke tests
mvn test -Dgroups=smoke

# Run only regression tests
mvn test -Dgroups=regression

# Run multiple groups at once (OR logic)
mvn test -Dgroups=smoke,api

# Exclude a group
mvn test -Dgroups=regression -DexcludedGroups=e2e
```
## Environment Configuration

Base URLs and other environment-specific settings are externalized into
per-environment properties files, so the same test suite can run against
different environments without touching the code. 

This pattern allows CI to run the same suite against qa on every commit and against staging before a release, without any code changes.

Properties are loaded from `src/test/resources/config-{env}.properties`.
If `-Denv` is not provided, `qa` is used by default.

| Environment | Properties file           | Notes                          |
|-------------|----------------------------|---------------------------------|
| `qa`        | `config-qa.properties`     | Default environment (demoqa.com) |

### Running tests against a specific environment

```bash
# Explicit (same as default)
mvn test -Denv=qa

# Combine with group filtering
mvn test -Dgroups=smoke -Denv=qa
```

### Overriding a single property

Any individual property can be overridden on the command line without
switching the whole environment file:

```bash
mvn test -Dapi.base.url=https://staging.demoqa.com
```

### Adding a new environment

1. Create `src/test/resources/config-{env}.properties` with the same keys
   as `config-qa.properties` (`ui.base.url`, `api.base.url`, `ui.path.books`,
   `ui.path.login`, `ui.path.profile`).
2. Run with `-Denv={env}`.

## Logging & Reporting
- Request/response logging via Log4j2, configured in `log4j2.xml`
- Failed requests are logged automatically via `log().ifValidationFails()`
- Full request/response history attached to Allure reports for every test run

After running tests, generate and view the Allure report:

```bash
mvn clean test
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