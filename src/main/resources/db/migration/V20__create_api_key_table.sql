CREATE TABLE api_key
(
    key          VARCHAR(255)             NOT NULL,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    user_id      VARCHAR(255),
    expires_at   TIMESTAMP WITH TIME ZONE,
    last_used    TIMESTAMP WITH TIME ZONE,
    last_used_ip VARCHAR(255),
    CONSTRAINT pk_apikey PRIMARY KEY (key)
);
