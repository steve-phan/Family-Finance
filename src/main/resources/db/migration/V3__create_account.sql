CREATE TABLE account (
    id          BIGSERIAL PRIMARY KEY,
    member_id   BIGINT        NOT NULL REFERENCES family_member (id),
    family_id   BIGINT        NOT NULL REFERENCES family (id),
    balance     NUMERIC(19, 2) NOT NULL DEFAULT 0,
    currency    VARCHAR(3)    NOT NULL,
    version     BIGINT        NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ   NOT NULL,
    updated_at  TIMESTAMPTZ   NOT NULL,
    CONSTRAINT uq_account_member UNIQUE (member_id),
    CONSTRAINT ck_account_balance_non_negative CHECK (balance >= 0)
);

CREATE INDEX idx_account_family_id ON account (family_id);
