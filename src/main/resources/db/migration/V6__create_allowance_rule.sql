CREATE TABLE allowance_rule (
    id           BIGSERIAL PRIMARY KEY,
    family_id    BIGINT         NOT NULL REFERENCES family (id),
    parent_id    BIGINT         NOT NULL REFERENCES family_member (id),
    child_id     BIGINT         NOT NULL REFERENCES family_member (id),
    amount       NUMERIC(19, 2) NOT NULL,
    period       VARCHAR(20)    NOT NULL,
    active       BOOLEAN        NOT NULL DEFAULT TRUE,
    next_run_at  TIMESTAMPTZ    NOT NULL,
    created_at   TIMESTAMPTZ    NOT NULL,
    updated_at   TIMESTAMPTZ    NOT NULL,
    CONSTRAINT ck_allowance_amount_positive CHECK (amount > 0),
    CONSTRAINT ck_allowance_period CHECK (period IN ('WEEKLY', 'MONTHLY'))
);

CREATE INDEX idx_allowance_due ON allowance_rule (active, next_run_at);
