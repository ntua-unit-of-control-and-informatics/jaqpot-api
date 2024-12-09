CREATE TABLE docker_config
(
    id           BIGSERIAL                NOT NULL,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    model_id     BIGINT                   NOT NULL,
    app_name     VARCHAR(255)             NOT NULL,
    docker_image VARCHAR(255),
    CONSTRAINT pk_docker_config PRIMARY KEY (id)
);

ALTER TABLE docker_config
    ADD CONSTRAINT uc_docker_config_appname UNIQUE (app_name);

ALTER TABLE docker_config
    ADD CONSTRAINT uc_docker_config_model UNIQUE (model_id);

ALTER TABLE docker_config
    ADD CONSTRAINT FK_DOCKER_CONFIG_ON_MODEL FOREIGN KEY (model_id) REFERENCES model (id);
