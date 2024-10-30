CREATE TABLE model_transformer
(
    id               BIGSERIAL                NOT NULL,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    model_id         BIGINT                   NOT NULL,
    transformer_type VARCHAR(255)             NOT NULL,
    name             VARCHAR(255)             NOT NULL,
    config           JSONB,
    CONSTRAINT pk_modeltransformer PRIMARY KEY (id)
);

ALTER TABLE model_transformer
    ADD CONSTRAINT FK_MODELTRANSFORMER_ON_MODEL FOREIGN KEY (model_id) REFERENCES model (id);

