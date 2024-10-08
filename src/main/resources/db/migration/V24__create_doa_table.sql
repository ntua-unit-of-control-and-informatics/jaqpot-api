CREATE TABLE doa
(
    id         BIGSERIAL                NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    model_id   BIGSERIAL                NOT NULL,
    method     VARCHAR(255)             NOT NULL,
    raw_doa    BYTEA,
    CONSTRAINT pk_doa PRIMARY KEY (id)
);

ALTER TABLE doa
    ADD CONSTRAINT FK_DOA_ON_MODEL FOREIGN KEY (model_id) REFERENCES model (id);
