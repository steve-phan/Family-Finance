CREATE TABLE family_member (
    id             BIGSERIAL PRIMARY KEY,
    family_id      BIGINT       NOT NULL REFERENCES family (id),
    email          VARCHAR(255) NOT NULL,
    password_hash  VARCHAR(255) NOT NULL,
    first_name     VARCHAR(80)  NOT NULL,
    last_name      VARCHAR(80)  NOT NULL,
    role           VARCHAR(20)  NOT NULL,
    enabled        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMPTZ  NOT NULL,
    updated_at     TIMESTAMPTZ  NOT NULL,
    CONSTRAINT uq_family_member_email UNIQUE (email),
    CONSTRAINT ck_family_member_role CHECK (role IN ('PARENT', 'CHILD'))
);

CREATE INDEX idx_family_member_family_id ON family_member (family_id);
