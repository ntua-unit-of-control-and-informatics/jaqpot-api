CREATE TABLE user_settings
(
    id               BIGSERIAL                NOT NULL,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    user_id          VARCHAR(255)             NOT NULL,
    dark_mode        BOOLEAN,
    collapse_sidebar BOOLEAN,
    CONSTRAINT pk_usersettings PRIMARY KEY (id)
);

ALTER TABLE user_settings
    ADD CONSTRAINT uc_user_settings_user_id UNIQUE (user_id);
