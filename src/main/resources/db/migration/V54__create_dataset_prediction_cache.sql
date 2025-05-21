ALTER TABLE model
    ADD supports_prediction_caching BOOLEAN NOT NULL DEFAULT false;

CREATE TABLE dataset_prediction_cache
(
    id         BIGSERIAL PRIMARY KEY,
    model_id   VARCHAR(255)             NOT NULL,
    input_hash VARCHAR(32)              NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    dataset_id BIGINT                   NOT NULL,
    CONSTRAINT fk_dataset_prediction_cache_dataset
        FOREIGN KEY (dataset_id)
            REFERENCES dataset (id)
            ON DELETE CASCADE
);

CREATE INDEX idx_dataset_prediction_cache_model_hash
    ON dataset_prediction_cache (model_id, input_hash);
