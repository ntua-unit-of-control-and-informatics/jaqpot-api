BEGIN;

-- Step 1: Add the new id column (without constraint yet)
ALTER TABLE organization_users
    ADD COLUMN association_type VARCHAR(255),
    ADD COLUMN created_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    ADD COLUMN updated_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    ADD COLUMN id               BIGSERIAL;

-- Step 2: Ensure the new column has unique values
UPDATE organization_users
SET id = nextval('organization_users_id_seq');

-- Step 3: Add the new primary key constraint
ALTER TABLE organization_users
    ADD CONSTRAINT pk_organization_users_id PRIMARY KEY (id);

COMMIT;

SELECT setval('organization_users_id_seq', (SELECT MAX(id) FROM organization_users));
