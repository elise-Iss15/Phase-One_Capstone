# IgirePay Payment Gateway

Backend Phase 1 Capstone — secure desktop payment platform using **JavaFX**, **JDBC**, **PostgreSQL**, and **OOP** (DAO + service layers).

## Project structure

| Layer | Package | Purpose |
|-------|---------|---------|
| Model | `com.igirepay.model` | `Account`, `WalletAccount`, `SavingsAccount`, `Customer`, `Transaction` |
| DAO | `com.igirepay.dao` | JDBC CRUD + idempotency (`ProcessedRequestDAO`) |
| Service | `com.igirepay.service` | `PaymentService`, `PaymentManager` (collections) |
| Console | `com.igirepay.console` | Lab 3 menu application |
| Report | `com.igirepay.report` | CSV export, daily summary, statements |
| UI | `com.igirepay.controller` | JavaFX login/dashboard (optional) |

## Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL 14+

## Database setup

1. Create database:
   ```bash
   createdb -U postgres igirepay
   ```
2. Apply schema:
   ```bash
   psql -U postgres -d igirepay -f sql/schema.sql
   ```
3. Copy `src/main/resources/application.properties.example` to `application.properties` and set your credentials.

## Run console app (Lab 3)

```bash
mvn compile exec:java
```

## Run JavaFX UI

```bash
mvn clean javafx:run
```

## Labs covered

- **Lab 1**: Inheritance (`WalletAccount`, `SavingsAccount`), polymorphic `deposit`/`withdraw`/`processTransaction`, collections in `PaymentManager`, duplicate detection via `Set` of reference IDs.
- **Lab 2**: PostgreSQL schema, DAO pattern, `PreparedStatement`, idempotency in `processed_requests`.
- **Lab 3**: Menu-driven console, exception handling, CSV reports, PIN auth, Git workflow (use feature branches for PRs).

## Idempotency

Every deposit, withdrawal, or transfer requires a unique **reference ID**. The first request is processed; repeats with the same ID are rejected and logged.

## Git workflow (Lab 3.5)

```bash
git checkout -b feature/customer-crud
# ... work, commit ...
git push -u origin feature/customer-crud
gh pr create --title "Add customer CRUD" --body "..."
```

## Submission

Push to GitHub and submit the repository link with a short demo of console flows: register customer, open wallet, deposit, transfer with reference ID, retry duplicate (rejected).
