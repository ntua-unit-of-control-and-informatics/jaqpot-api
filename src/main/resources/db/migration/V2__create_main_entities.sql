CREATE TABLE model
(
    id               BIGSERIAL                   NOT NULL,
    created_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    user_id          VARCHAR(255),
    meta             JSONB,
    public           BOOLEAN,
    type             VARCHAR(255),
    jaqpotpy_version VARCHAR(255)                NOT NULL,
    reliability      INTEGER,
    pretrained       BOOLEAN,
    actual_model     bytea                       NOT NULL,
    CONSTRAINT pk_model PRIMARY KEY (id)
);

CREATE TABLE feature
(
    id                 BIGSERIAL                   NOT NULL,
    created_at         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    model_id           BIGINT                      NOT NULL,
    name               VARCHAR(255)                NOT NULL,
    feature_dependency VARCHAR(255)                NOT NULL,
    feature_type       VARCHAR(255)                NOT NULL,
    meta               JSONB,
    visible            BOOLEAN,
    sort_order         INTEGER,
    CONSTRAINT pk_feature PRIMARY KEY (id)
);

CREATE TABLE library
(
    id         BIGSERIAL                   NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    model_id   BIGINT,
    name       VARCHAR(255),
    version    VARCHAR(255),
    CONSTRAINT pk_library PRIMARY KEY (id)
);

ALTER TABLE feature
    ADD CONSTRAINT FK_FEATURE_ON_MODEL FOREIGN KEY (model_id) REFERENCES model (id);

ALTER TABLE library
    ADD CONSTRAINT FK_LIBRARY_ON_MODEL FOREIGN KEY (model_id) REFERENCES model (id);
