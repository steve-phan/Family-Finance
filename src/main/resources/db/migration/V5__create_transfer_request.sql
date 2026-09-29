CREATE TABLE transfer_request (
    id            BIGSERIAL PRIMARY KEY,
    family_id     BIGINT         NOT NULL REFERENCES family (id),
    requester_id  BIGINT         NOT NULL REFERENCES family_member (id),
    payer_id      BIGINT         NOT NULL REFERENCES family_member (id),
    amount        NUMERIC(19, 2) NOT NULL,
    status        VARCHAR(20)    NOT NULL,
    message       VARCHAR(255),
    created_at    TIMESTAMPTZ    NOT NULL,
    updated_at    TIMESTAMPTZ    NOT NULL,
    resolved_at   TIMESTAMPTZ,
    CONSTRAINT ck_request_amount_positive CHECK (amount > 0),
    CONSTRAINT ck_request_distinct_members CHECK (requester_id <> payer_id),
    CONSTRAINT ck_request_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED'))
);

CREATE INDEX idx_request_payer_status ON transfer_request (payer_id, status);
CREATE INDEX idx_request_requester ON transfer_request (requester_id);
