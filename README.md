# Family Finance

Backend API for a family wallet. A parent can send money to children. Children can send money to siblings and to parents. Built with **Java 17**, **Spring Boot 3.4**, and **PostgreSQL**.

## What it does

- A parent registers a family and receives a wallet
- The parent adds children (or another parent); each member gets an account
- The parent deposits external funds, then transfers to children
- Children transfer to each other inside the same family
- Children can request money; the payer approves or rejects
- Parents can set weekly or monthly allowances
- Each family is a tenant. Hibernate `@TenantId` on `family_id` filters every member, account, transfer, request, allowance, and audit row
- JWT binds the tenant for the request; login and family registration run as the unscoped root tenant
- Money movement uses `READ_COMMITTED`, a 10s timeout, pessimistic write locks in a stable member-id order, and `@Version` on accounts
- Account locks and audit writes require an existing transaction (`MANDATORY`) so they cannot commit on their own
- Allowance payouts run in `REQUIRES_NEW` per child, with the child's family tenant bound, so one failure does not roll back the others
- Idempotency keys make retries safe

Transfers never leave the family. Balances cannot go negative.

## Run locally

Java 17 and Docker are required.

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
docker compose up -d
mvn spring-boot:run
```

API docs: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## Tests

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
mvn test
```

Unit tests cover money rules and transfer policy. The integration test uses Testcontainers PostgreSQL and walks through parent → child and child → child transfers.

## API sketch

| Method | Path | Who |
| --- | --- | --- |
| POST | `/api/auth/register-family` | public |
| POST | `/api/auth/login` | public |
| GET | `/api/me` | any member |
| POST | `/api/family/members` | parent |
| POST | `/api/accounts/me/deposits` | parent |
| POST | `/api/transfers` | any member |
| GET | `/api/transfers` | parent: family; child: own |
| POST | `/api/transfer-requests` | any member |
| POST | `/api/transfer-requests/{id}/approve` | payer |
| POST | `/api/allowances` | parent |
| GET | `/api/audit-events` | parent |

Example: register, deposit, send.

```bash
TOKEN=$(curl -s -X POST localhost:8080/api/auth/register-family \
  -H 'Content-Type: application/json' \
  -d '{"familyName":"Muller","currency":"EUR","email":"anna@example.com","password":"secret123","firstName":"Anna","lastName":"Muller"}' \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['token'])")

curl -s -X POST localhost:8080/api/accounts/me/deposits \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"amount": 100.00}'
```

## Layout

```
src/main/java/com/familyfinance
  api/          REST controllers and DTOs
  config/       JWT, OpenAPI, JPA tenancy
  domain/       JPA model (Family is the tenant root)
  exception/    typed business errors
  repository/   Spring Data
  security/     JWT filter binds TenantContext
  service/      transfers, requests, allowances
  tenant/       ThreadLocal tenant + Hibernate resolver
  tx/           @MoneyTransaction
src/main/resources/db/migration   Flyway
```

Money uses `BigDecimal` with scale 2. Account rows have `CHECK (balance >= 0)` and a Hibernate `@Version` column.
