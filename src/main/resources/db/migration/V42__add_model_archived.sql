ALTER TABLE model
    ADD archived BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE model
    ADD archived_at TIMESTAMP WITH TIME ZONE;
