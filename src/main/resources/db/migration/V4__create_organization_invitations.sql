CREATE TABLE organization_invitation
(
    id              UUID                        NOT NULL,
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    user_id         VARCHAR(255)                NOT NULL,
    user_email      VARCHAR(255)                NOT NULL,
    organization_id BIGINT                      NOT NULL,
    status          VARCHAR(255)                NOT NULL,
    expiration_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_organization_invitation PRIMARY KEY (id)
);

ALTER TABLE organization_invitation
    ADD CONSTRAINT FK_ORGANIZATION_INVITATION_ON_ORGANIZATION FOREIGN KEY (organization_id) REFERENCES organization (id);

