CREATE TABLE model
(
    id                        BIGSERIAL                NOT NULL,
    legacy_id                 VARCHAR,
    created_at                TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at                TIMESTAMP WITH TIME ZONE NOT NULL,
    creator_id                VARCHAR(255)             NOT NULL,
    name                      VARCHAR(255)             NOT NULL,
    type                      VARCHAR(255)             NOT NULL,
    jaqpotpy_version          VARCHAR(255)             NOT NULL,
    visibility                VARCHAR(255)             NOT NULL,
    description               TEXT,
    meta                      JSONB,
    legacy_prediction_service varchar,
    pretrained                BOOLEAN,
    actual_model              bytea                    NOT NULL,
    CONSTRAINT pk_model PRIMARY KEY (id)
);

CREATE TABLE feature
(
    id                 BIGSERIAL                NOT NULL,
    created_at         TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at         TIMESTAMP WITH TIME ZONE NOT NULL,
    model_id           BIGINT                   NOT NULL,
    key                VARCHAR(255)             NOT NULL,
    name               VARCHAR(255)             NOT NULL,
    description        TEXT,
    feature_dependency VARCHAR(255)             NOT NULL,
    feature_type       VARCHAR(255)             NOT NULL,
    meta               JSONB,
    visible            BOOLEAN,
    sort_order         INTEGER,
    possible_values    JSONB,
    CONSTRAINT pk_feature PRIMARY KEY (id)
);

CREATE TABLE library
(
    id         BIGSERIAL                NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    model_id   BIGINT                   NOT NULL,
    name       VARCHAR(255)             NOT NULL,
    version    VARCHAR(255)             NOT NULL,
    CONSTRAINT pk_library PRIMARY KEY (id)
);

CREATE TABLE dataset
(
    id             BIGSERIAL                NOT NULL,
    created_at     TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at     TIMESTAMP WITH TIME ZONE NOT NULL,
    model_id       BIGINT                   NOT NULL,
    user_id        VARCHAR(255)             NOT NULL,
    type           VARCHAR(255)             NOT NULL,
    status         VARCHAR(255)             NOT NULL,
    failure_reason TEXT,
    CONSTRAINT pk_dataset PRIMARY KEY (id)
);

CREATE TABLE data_entry
(
    id         BIGSERIAL                NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    dataset_id BIGINT                   NOT NULL,
    type       VARCHAR(255)             NOT NULL,
    role       VARCHAR(255)             NOT NULL,
    values     JSONB                    NOT NULL,
    CONSTRAINT pk_dataentry PRIMARY KEY (id)
);

ALTER TABLE feature
    ADD CONSTRAINT FK_FEATURE_ON_MODEL FOREIGN KEY (model_id) REFERENCES model (id);

ALTER TABLE library
    ADD CONSTRAINT FK_LIBRARY_ON_MODEL FOREIGN KEY (model_id) REFERENCES model (id);

ALTER TABLE dataset
    ADD CONSTRAINT FK_DATASET_ON_MODEL FOREIGN KEY (model_id) REFERENCES model (id);

ALTER TABLE data_entry
    ADD CONSTRAINT FK_DATAENTRY_ON_DATASET FOREIGN KEY (dataset_id) REFERENCES dataset (id);
