ALTER TABLE model
    DROP CONSTRAINT fk_model_on_associated_organization;

ALTER TABLE organization_models
    ADD association_type VARCHAR(255);

ALTER TABLE organization_models
    ADD created_at TIMESTAMP WITH TIME ZONE NOT NULL default CURRENT_TIMESTAMP;

ALTER TABLE organization_models
    ADD updated_at TIMESTAMP WITH TIME ZONE NOT NULL default CURRENT_TIMESTAMP;

ALTER TABLE organization_models
    ALTER COLUMN association_type SET NOT NULL;

ALTER TABLE model
    DROP COLUMN associated_organization_id;

ALTER TABLE organization_models
    ADD CONSTRAINT FK_MODEL_ON_ASSOCIATED_ORGANIZATION FOREIGN KEY (organization_id) REFERENCES organization (id);

ALTER TABLE organization_models
    ADD CONSTRAINT FK_MODEL_ON_ASSOCIATED_MODEL FOREIGN KEY (model_id) REFERENCES model (id);
