ALTER TABLE feature
    ADD units VARCHAR(255);

ALTER TABLE model
    ALTER COLUMN actual_model DROP NOT NULL;

ALTER TABLE model
    ALTER COLUMN legacy_id TYPE VARCHAR(255) USING (legacy_id::VARCHAR(255));

ALTER TABLE model
    ALTER COLUMN legacy_prediction_service TYPE VARCHAR(255) USING (legacy_prediction_service::VARCHAR(255));
