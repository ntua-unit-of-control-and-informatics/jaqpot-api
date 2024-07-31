ALTER TABLE organization_models
    DROP CONSTRAINT pk_organization_models;

DELETE
FROM organization_models;

ALTER TABLE organization_models
    ADD CONSTRAINT pk_organization_models PRIMARY KEY (organization_id, model_id, association_type);
