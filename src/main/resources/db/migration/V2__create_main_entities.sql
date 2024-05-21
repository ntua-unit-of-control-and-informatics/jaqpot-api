CREATE TABLE model
(
    id               BIGSERIAL NOT NULL,
    created_at       TIMESTAMP WITHOUT TIME ZONE,
    updated_at       TIMESTAMP WITHOUT TIME ZONE,
    meta             JSONB,
    public           BOOLEAN   NOT NULL,
    type             VARCHAR(255),
    jaqpotpy_version VARCHAR(255),
    reliability      INTEGER   NOT NULL,
    pretrained       BOOLEAN   NOT NULL,
    actual_model     bytea,
    CONSTRAINT pk_model PRIMARY KEY (id)
);

CREATE TABLE feature
(
    id           BIGSERIAL NOT NULL,
    created_at   TIMESTAMP WITHOUT TIME ZONE,
    updated_at   TIMESTAMP WITHOUT TIME ZONE,
    model_id     BIGINT,
    name         VARCHAR(255),
    feature_type VARCHAR(255),
    meta         JSONB,
    visible      BOOLEAN,
    CONSTRAINT pk_feature PRIMARY KEY (id)
);

CREATE TABLE library
(
    id         BIGSERIAL NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    model_id   BIGINT,
    name       VARCHAR(255),
    version    VARCHAR(255),
    CONSTRAINT pk_library PRIMARY KEY (id)
);

ALTER TABLE feature
    ADD CONSTRAINT FK_FEATURE_ON_MODEL FOREIGN KEY (model_id) REFERENCES model (id);

ALTER TABLE library
    ADD CONSTRAINT FK_LIBRARY_ON_MODEL FOREIGN KEY (model_id) REFERENCES model (id);
