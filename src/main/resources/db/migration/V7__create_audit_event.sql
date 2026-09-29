CREATE TABLE audit_event (
    id           BIGSERIAL PRIMARY KEY,
    family_id    BIGINT      NOT NULL REFERENCES family (id),
    actor_id     BIGINT REFERENCES family_member (id),
    action       VARCHAR(40) NOT NULL,
    entity_type  VARCHAR(40) NOT NULL,
    entity_id    BIGINT,
    detail       VARCHAR(500),
    created_at   TIMESTAMPTZ NOT NULL,
    updated_at   TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_audit_family_created ON audit_event (family_id, created_at DESC);
