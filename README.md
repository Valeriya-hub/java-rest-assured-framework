# Book Store Automation Tests
[![Run Automation Tests](https://github.com/Valeriya-hub/java-rest-assured-framework/actions/workflows/tests.yml/badge.svg?branch=main)](https://github.com/Valeriya-hub/java-rest-assured-framework/actions/workflows/tests.yml)
[![Nightly Regression](https://github.com/Valeriya-hub/java-rest-assured-framework/actions/workflows/nightly.yml/badge.svg?branch=main)](https://github.com/Valeriya-hub/java-rest-assured-framework/actions/workflows/nightly.yml)
![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)
![Selenide](https://img.shields.io/badge/Selenide-7.17.0-blue)
![RestAssured](https://img.shields.io/badge/REST--Assured-5.5.0-green)
![Allure](https://img.shields.io/badge/Allure-2.29.0-red?logo=qameta)

This project contains automated tests for the Book Store application using a hybrid approach combining UI and API testing.

## 📊 Test Reports
- [Latest report (main, Chrome, full suite)](https://valeriya-hub.github.io/java-rest-assured-framework/latest/)
- Nightly regression: [Chrome](https://valeriya-hub.github.io/java-rest-assured-framework/nightly/chrome/) · [Firefox](https://valeriya-hub.github.io/java-rest-assured-framework/nightly/firefox/) · [Edge](https://valeriya-hub.github.io/java-rest-assured-framework/nightly/edge/)

Reports include trend history and failure categories (product defects, test defects, infrastructure).

## Tech Stack

- **Java 21** - Programming language
- **Maven** - Build tool and dependency management
- **Selenide 7.17.0** - UI automation framework (wrapper around Selenium WebDriver)
- **TestNG 7.10.2** - Test framework
- **Rest Assured 5.5.0** - API testing library
- **Allure 2.29.0** - Test reporting framework
- **AssertJ 3.27.7** - Assertion library
- **Lombok 1.18.34** - Code generation for boilerplate reduction
- **Jackson 2.22.4** - JSON processing
- **Log4j2** - request/response logging та GitHub Actions + Dependabot - CI and dependency updates.

## Project Structure

```
java-rest-assured-framework/
├── .github/
│   ├── allure/
│   │   └── categories.json
│   ├── pages/
│   │   └── index.html
│   └── workflows/
│       ├── nightly.yml
│       ├── tests.yml
│       └── dependabot.yml
├── src/
│   ├── main/
│   │   └── java/
│   │       ├── api/
│   │       │   ├── client/         # API clients (Rest Assured)
│   │       │   └── model/          # POJO request/response models
│   │       ├── auth/               # Session & token management
│   │       ├── config/             # Configurations (ConfigReader, Browser, Logging)
│   │       │   └── constants/      # Constants (API, UI, Auth)
│   │       └── ui/                 # Page Object models (Selenide)
│   └── test/
│       ├── java/
│       │   ├── api.assertions/     # Custom API assertions
│       │   ├── data/               # Test data generators (Data Providers)
│       │   ├── listener/           # Allure listeners & custom attachments
│       │   └── tests/
│       │       ├── api/            # API tests
│       │       ├── base/           # Base test classes
│       │       ├── e2e/            # End-to-End scenarios (API + UI)
│       │       └── ui/             # UI tests (Selenide)
│       └── resources/              # Configuration files (properties, log4j2)
├── pom.xml
├── testng.xml
└── README.md
```

## Test Coverage

### API Tests (`tests.api`)
* **`CreateUserApiTest`**: Verifies user registration functionality, required fields validation, and error handling for invalid payloads.
* **`GenerateTokenApiTest`**: Validates authorization token generation and response status codes for valid/invalid credentials.
* **`GetUserApiTest`**: Checks fetching account details and user profile state using authorized API requests.
* **`LoginApiTest`**: Verifies API-level authentication and proper payload contracts.

### UI Tests (`tests.ui`)
* **`BookDetailsTest`**: Verifies book details view, correct metadata display, and UI element rendering on the book details page.
* **`BookSearchTest`**: Validates search filtering functionality and real-time results updates in the Book Store.
* **`BookStoreTest`**: Checks initial state, layout rendering, and pagination elements on the main Book Store page.
* **`GuestAccessTest`**: Verifies UI behavior, visibility of restrictions, and login button availability for unauthenticated visitors.

### End-to-End (E2E) Tests (`tests.e2e`)
* **`AddBookToCollectionTest`**: Combines API-based user setup with UI verification of adding a book to the user's profile collection.
* **`AuthorizationTest`**: Validates full authorization flows, including login, session management, profile icon rendering, and logout actions.
* **`DeleteBookFromCollectionTest`**: Verifies end-to-end user workflows for managing and removing books from a personal collection.

## Configuration

Settings live in `src/test/resources/config-{env}.properties` (default env: `qa`):

```properties
# Base URLs
ui.base.url=https://demoqa.com
api.base.url=https://demoqa.com

# UI routes
ui.path.books=/books
ui.path.login=/login
ui.path.profile=/profile

# Browser
browser=chrome
browser.size=1920x1080
headless.enabled=false

# Timeouts
ui.timeout.ms=8000
ui.polling.ms=200
ui.pageload.timeout.ms=45000
long.operation.timeout.sec=20
```

Any property can be overridden with `-D` (precedence: `-D` → properties file → default).

| Option | Default | Notes |
|--------|---------|-------|
| `-Dbrowser` | `chrome` | `chrome`, `firefox`, `edge` |
| `-Dheadless` | `false` locally, `true` when `CI` is set | |
| `-Dui.pageload.strategy` | `eager` | `normal`, `eager`, `none` |
| `-Dbrowser.no.sandbox` | `false` | CI-only fallback for Chromium sandbox problems |

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

### Run tests in Headless mode:
```bash
mvn clean test -Dheadless=true
```

## Test Groups

Tests are organized into TestNG groups so they can be run selectively,
without executing the full suite every time. 

Groups let CI run `smoke` on every pull request for fast feedback, the full suite on every push to `main`,
and `regression` nightly across browsers.

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

## Cross-browser execution

Supported browsers: **Chrome** (default), **Firefox**, **Edge**.

```bash
mvn test -Dbrowser=firefox
mvn test -Dbrowser=edge -Dheadless=true
```

| Trigger | Browsers | Scope | Report |
|---------|----------|-------|--------|
| Pull request | Chrome | `smoke` | workflow artifact |
| Push to `main` | Chrome | full suite | `/latest/` on GitHub Pages |
| Nightly (02:00 UTC) / manual | Chrome, Firefox, Edge (matrix) | `regression` (configurable) | `/nightly/<browser>/` |

Manual runs (`workflow_dispatch`) let you choose the browser, TestNG group and environment.
Reports are published only from the default branch.

**Page load strategy.** UI tests use `eager`: the public demo site loads ads and trackers that make
the full `load` event slow and unreliable on CI runners. Selenide waits explicitly for the elements
each test needs.

> **Note on Edge in CI:** Edge run with `--no-sandbox` flag in the GitHub Actions Linux environment to bypass AppArmor security restrictions and container memory limits.

## Environment Configuration

Base URLs and other environment-specific settings are externalized into per-environment properties files, so the same test suite can run against different environments without touching the code. 

The same suite can run against different environments by switching -Denv; currently only qa is configured.

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

1. Copy config-qa.properties and change the values.
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
allure generate target/allure-results --clean -o target/allure-report
allure open target/allure-report
```

In CI the report additionally contains:
- **Environment widget**: browser, headless mode, environment, OS, Java version;
- **Trend history**, carried over between runs via the `gh-pages` branch;
- **Categories**: known product defects (skipped `KnownIssue` tests), infrastructure problems, product defects, test defects.

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

## Dependency security

Dependencies are scanned by IntelliJ (Mend.io) and Dependabot. Direct dependencies are kept
up to date. Remaining warnings come from transitive libraries of Selenium/Selenide, RestAssured
and Allure; the framework is a test tool that runs against a public demo application and does
not process untrusted XML or polymorphic JSON, so the practical risk is low. Dependabot PRs
are verified by the CI suite before merging.

Dependabot opens weekly grouped PRs (Allure modules and Maven plugins are updated together); major Allure upgrades are ignored and done deliberately. Every PR runs the smoke suite on Chrome before merging.

## Findings in the application under test

While automating the demoqa API, the following behaviours were observed.
They are application defects (or deviations from common API conventions),
not framework problems.

| #  | Endpoint | Observation | Why it matters | How the framework handles it |
|----|----------|-------------|----------------|------------------------------|
| 1  | `POST /Account/v1/Login` | The response body contains the user's password in plain text. | Sensitive data exposure (OWASP API3:2023, Broken Object Property Level Authorization). An API must never return passwords. | `LoginResponse` deliberately does not map `password`; the test `loginResponseDoesNotExposePassword` asserts the desired behaviour via `KnownIssue.expectFailure` (skipped while the defect exists, fails once it is fixed). |
| 2  | `DELETE /Account/v1/User/{id}` for an already deleted user | Responds with HTTP 200 and an error body (code 1207) instead of a 4xx status. | Clients cannot rely on status codes to detect errors. | Cleanup treats only 204 and 401 as expected and fails on anything else. |
| 3  | `POST /Account/v1/User` with an existing username | Responds with 406 Not Acceptable; 409 Conflict would be conventional. | Non-standard status codes complicate client code. | `ApiErrors.USER_EXISTS` documents the actual contract. |
| 4  | `POST /Account/v1/Login` with empty fields | Responds with HTTP 200 OK and an empty body instead of a 4xx validation error status. | Masks validation errors behind a success status code, making standard error handling impossible. | Tests explicitly assert HTTP 200 and a blank response body for these invalid payload scenarios. |
| 5  | `POST /Account/v1/Login` with a missing field | Responds with HTTP 500 and an HTML page containing a stack trace: internal file paths, the ORM and database in use, and the failing query condition. | Unhandled exception on malformed input and information disclosure (CWE-209, OWASP API8:2023). `POST /Account/v1/User` handles the same input correctly (400), so validation is inconsistent. | The test asserts the desired behaviour inside `KnownIssue.expectFailure(...)`: it is reported as skipped while the defect exists and fails once it is fixed. |
| 6  | `GET /Account/v1/User/{id}` with a non-existing id or the id of another user | Responds with HTTP 401 and "User not found!" (code 1207). | 401 means "not authenticated", but the caller is authenticated; 404 (or 403) would be conventional. A client may mistake a missing resource for an expired session and refresh the token or log the user out. | `ApiError.USER_NOT_FOUND` documents the observed contract and the tests pin it. Both cases return an identical response, so no data of other users is exposed and the API does not reveal whether an id exists. |
| 7  | `GET /BookStore/v1/Book` without the `ISBN` parameter | Responds with HTTP 500 and an HTML page containing a stack trace with internal file paths, technology stack (Node.js, Sequelize, MySQL), and the failing query condition. | Unhandled exception on missing input validation and information disclosure (CWE-209, OWASP API8:2023). | `GetBookApiTest.getBookWithoutIsbnParameter` asserts the desired behaviour inside `KnownIssue.expectFailure(...)`: it is reported as skipped while the defect exists and fails once it is fixed. |
| 8  | `POST /BookStore/v1/Books` without the `userId` field | Responds with HTTP 500 and an HTML page containing a stack trace with internal file paths, the ORM and database in use (Sequelize, MySQL) and the failing query condition (`WHERE parameter "user_id" has invalid "undefined" value`). | Unhandled exception on missing input validation and information disclosure (CWE-209, OWASP API8:2023). An empty or unknown `userId` is handled correctly (401, code 1207), so validation is inconsistent. | `AddBookApiTest.addBookWithoutUserId` pins the current behaviour via `ApiAssertions.assertServerErrorHtmlKnownDeviation` (status 500, text/html). After the fix, switch it to `assertApiError(response, ApiError.USER_ID_NOT_CORRECT)`. |
| 9  | `POST /BookStore/v1/Books` without the `userId` field | Responds with HTTP 500 and an HTML page containing a stack trace with internal file paths, the ORM and database in use (Sequelize, MySQL) and the failing query condition (`WHERE parameter "user_id" has invalid "undefined" value`). | Unhandled exception on missing input validation and information disclosure (CWE-209, OWASP API8:2023). An empty or unknown `userId` is handled correctly (401, code 1207), so validation is inconsistent. | `AddBookApiTest.addBookWithoutUserId` asserts the desired behaviour inside `KnownIssue.expectFailure(...)`: it is reported as skipped while the defect exists and fails once it is fixed. |
| 10 | `POST /BookStore/v1/Books` without the `collectionOfIsbns` field | Responds with HTTP 500 and an HTML page containing a stack trace (`TypeError: Cannot read properties of undefined (reading 'length')`) and internal file paths. | The code reads `.length` of an absent field without a check (CWE-209, OWASP API8:2023). An empty array is handled correctly (400, code 1207), so a missing field and an empty value are treated differently. | `AddBookApiTest.addBookWithoutCollectionOfIsbns` asserts the desired behaviour inside `KnownIssue.expectFailure(...)`: it is reported as skipped while the defect exists and fails once it is fixed. |
| 11 | Error code `1207` across endpoints | The same code is used for different errors: “User Id not correct!” (401), “Collection of books required.” (400), “User not found!” (401), and the error of a repeated DELETE (200). | Clients cannot rely on the code field to identify an error type; they have to compare messages as well. | `assertApiError` compares status, code and message together, so the tests distinguish these errors. |
| 12 | `DELETE /BookStore/v1/Book` without `isbn` | Responds with HTTP 500 and an HTML page containing a stack trace with internal file paths, Sequelize ORM details, and the failing query condition (`WHERE parameter "isbn" has invalid "undefined" value`). | Unhandled exception on missing input validation and information disclosure (CWE-209, OWASP API8:2023). | `DeleteBookApiTest.deleteBookWithoutIsbn` asserts the desired behaviour inside `KnownIssue.expectFailure(...)`: it is reported as skipped while the defect exists and fails once it is fixed. |
| 13 | `DELETE /BookStore/v1/Book` without `userId` | Responds with HTTP 500 and an HTML page containing a stack trace with internal file paths and Sequelize ORM details (`WHERE parameter "user_id" has invalid "undefined" value`). | Unhandled exception on missing input validation and information disclosure (CWE-209, OWASP API8:2023). | `DeleteBookApiTest.deleteBookWithoutUserId` asserts the desired behaviour inside `KnownIssue.expectFailure(...)`: it is reported as skipped while the defect exists and fails once it is fixed. |
| 14 | **UI Authentication Flow & Firefox on CI** (`POST /Account/v1/Login` latency) | High response latency and intermittent slow redirects during UI authentication in Firefox containerized runners on GitHub Actions. | Default UI timeouts (e.g., 8s) cause flaky test failures specifically for `authorizedUserSeesUsernameAndProfileIcon` when waiting for redirect to `/profile`. | Avoided increasing the global Selenide timeout. Instead, applied explicit targeted wait in `ProfilePage.shouldBeOpened()` using `Config.longOperationTimeout()` to gracefully handle CI environment backend spikes without slowing down overall test suite execution. |

## CI/CD

- `tests.yml`: PR → `smoke` on Chrome; push to `main` → full suite + report on GitHub Pages.
- `nightly.yml`: `regression` on a Chrome/Firefox/Edge matrix, a separate report per browser.
- Least privilege: workflows run with read-only `contents`; only the publish job can write to `gh-pages`.
- Failure diagnostics: Allure attaches a screenshot to the failed step; Surefire reports are uploaded on failure.