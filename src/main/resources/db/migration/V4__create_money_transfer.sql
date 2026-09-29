CREATE TABLE money_transfer (
    id                 BIGSERIAL PRIMARY KEY,
    family_id          BIGINT         NOT NULL REFERENCES family (id),
    source_account_id  BIGINT         NOT NULL REFERENCES account (id),
    target_account_id  BIGINT         NOT NULL REFERENCES account (id),
    amount             NUMERIC(19, 2) NOT NULL,
    currency           VARCHAR(3)     NOT NULL,
    type               VARCHAR(40)    NOT NULL,
    status             VARCHAR(20)    NOT NULL,
    description        VARCHAR(255),
    initiated_by_id    BIGINT         NOT NULL REFERENCES family_member (id),
    idempotency_key    VARCHAR(64),
    created_at         TIMESTAMPTZ    NOT NULL,
    updated_at         TIMESTAMPTZ    NOT NULL,
    completed_at       TIMESTAMPTZ,
    CONSTRAINT ck_transfer_amount_positive CHECK (amount > 0),
    CONSTRAINT ck_transfer_distinct_accounts CHECK (source_account_id <> target_account_id)
);

CREATE INDEX idx_transfer_family_id ON money_transfer (family_id);
CREATE INDEX idx_transfer_source ON money_transfer (source_account_id);
CREATE INDEX idx_transfer_target ON money_transfer (target_account_id);
CREATE UNIQUE INDEX uq_transfer_idempotency
    ON money_transfer (initiated_by_id, idempotency_key)
    WHERE idempotency_key IS NOT NULL;
