CREATE TABLE api_key
(
    id            UUID                        NOT NULL,
    created_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    client_key    VARCHAR(255)                NOT NULL,
    client_secret VARCHAR(255)                NOT NULL,
    user_id       VARCHAR(255)                NOT NULL,
    note          VARCHAR(255),
    expires_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    enabled       BOOLEAN                     NOT NULL,
    last_used     TIMESTAMP WITHOUT TIME ZONE,
    last_used_ip  VARCHAR(255),
    CONSTRAINT pk_apikey PRIMARY KEY (id)
);

ALTER TABLE api_key
    ADD CONSTRAINT uc_api_key_client_key UNIQUE (client_key);
