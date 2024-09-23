CREATE SEQUENCE IF NOT EXISTS lead_id_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE lead
(
    id         BIGSERIAL                   NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    email      VARCHAR(255)                NOT NULL,
    name       VARCHAR(255),
    status     VARCHAR(255)                NOT NULL,
    CONSTRAINT pk_lead PRIMARY KEY (id)
);

ALTER TABLE lead
    ADD CONSTRAINT uc_lead_email UNIQUE (email);

