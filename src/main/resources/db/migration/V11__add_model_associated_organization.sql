ALTER TABLE model
    ADD associated_organization_id BIGINT;

ALTER TABLE model
    ADD CONSTRAINT FK_MODEL_ON_ASSOCIATED_ORGANIZATION FOREIGN KEY (associated_organization_id) REFERENCES organization (id);

