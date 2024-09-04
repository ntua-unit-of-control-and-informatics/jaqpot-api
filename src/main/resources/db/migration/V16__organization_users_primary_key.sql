BEGIN;

-- Step 1: Add the new id column (without constraint yet)
ALTER TABLE organization_users
    ADD COLUMN association_type VARCHAR(255) DEFAULT 'MEMBER ',
    ADD COLUMN created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT timezone('utc', now()),
    ADD COLUMN updated_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT timezone('utc', now()),
    ADD COLUMN id               BIGSERIAL;

-- Step 2: Verify that id has unique values and update the sequence if needed
SELECT setval('organization_users_id_seq', (SELECT MAX(id) FROM organization_users));


-- Step 3: Add the new primary key constraint
ALTER TABLE organization_users
    ADD CONSTRAINT pk_organization_users_id PRIMARY KEY (id);

COMMIT;

