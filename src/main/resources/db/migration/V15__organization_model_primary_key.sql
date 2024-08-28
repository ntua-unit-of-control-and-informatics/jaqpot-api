BEGIN;

-- Step 1: Add the new id column (without constraint yet)
ALTER TABLE organization_models
    ADD COLUMN id BIGSERIAL;

-- Step 2: Ensure the new column has unique values
UPDATE organization_models
SET id = nextval('organization_models_id_seq');

-- Step 3: Drop the old primary key constraint
ALTER TABLE organization_models
    DROP CONSTRAINT pk_organization_models;

-- Step 4: Add the new primary key constraint
ALTER TABLE organization_models
    ADD CONSTRAINT pk_organization_models_id PRIMARY KEY (id);

COMMIT;

-- Optional: If you want to reset the sequence to start after the max id
SELECT setval('organization_models_id_seq', (SELECT MAX(id) FROM organization_models));
