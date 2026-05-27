-- IgirePay PostgreSQL schema (Lab 2.1)
-- Create DB first:  createdb -U postgres Igiredb
-- Run: psql -U postgres -d Igiredb -f sql/schema.sql

DROP TABLE IF EXISTS processed_requests CASCADE;
DROP TABLE IF EXISTS transactions CASCADE;
DROP TABLE IF EXISTS accounts CASCADE;
DROP TABLE IF EXISTS customers CASCADE;

CREATE TABLE customers (
    id            BIGSERIAL PRIMARY KEY,
    full_name     VARCHAR(150) NOT NULL,
    email         VARCHAR(150) NOT NULL UNIQUE,
    phone_number  VARCHAR(30)  NOT NULL
);

CREATE TABLE accounts (
    id            BIGSERIAL PRIMARY KEY,
    customer_id   BIGINT       NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    account_type  VARCHAR(20)  NOT NULL CHECK (account_type IN ('WALLET', 'SAVINGS')),
    balance       NUMERIC(18, 2) NOT NULL DEFAULT 0 CHECK (balance >= 0),
    pin           INTEGER      NOT NULL DEFAULT 0,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE transactions (
    id               BIGSERIAL PRIMARY KEY,
    account_id       BIGINT       NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    reference_id     VARCHAR(100) NOT NULL,
    transaction_type VARCHAR(30)  NOT NULL,
    amount           NUMERIC(18, 2) NOT NULL CHECK (amount > 0),
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE processed_requests (
    id            BIGSERIAL PRIMARY KEY,
    reference_id  VARCHAR(100) NOT NULL UNIQUE,
    processed_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_transactions_account ON transactions(account_id);
CREATE INDEX idx_transactions_created ON transactions(created_at);
CREATE INDEX idx_accounts_customer ON accounts(customer_id);
