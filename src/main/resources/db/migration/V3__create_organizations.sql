CREATE TABLE organization
(
    id            BIGSERIAL                   NOT NULL,
    created_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    name          VARCHAR(255) UNIQUE         NOT NULL,
    creator_id    VARCHAR(255)                NOT NULL,
    contact_email VARCHAR(255)                NOT NULL,
    visibility    VARCHAR(255)                NOT NULL,
    description   TEXT,
    contact_phone VARCHAR(255),
    website       VARCHAR(255),
    address       VARCHAR(255),
    CONSTRAINT pk_organization PRIMARY KEY (id)
);

CREATE TABLE organization_models
(
    model_id        BIGINT NOT NULL,
    organization_id BIGINT NOT NULL,
    CONSTRAINT pk_organization_models PRIMARY KEY (model_id, organization_id)
);

CREATE TABLE organization_users
(
    organization_id BIGINT       NOT NULL,
    user_id         VARCHAR(255) NOT NULL
);

ALTER TABLE organization
    ADD CONSTRAINT uc_organization_name UNIQUE (name);

ALTER TABLE organization_users
    ADD CONSTRAINT fk_organization_users_on_organization FOREIGN KEY (organization_id) REFERENCES organization (id);

ALTER TABLE organization_models
    ADD CONSTRAINT fk_orgmod_on_model FOREIGN KEY (model_id) REFERENCES model (id);

ALTER TABLE organization_models
    ADD CONSTRAINT fk_orgmod_on_organization FOREIGN KEY (organization_id) REFERENCES organization (id);
