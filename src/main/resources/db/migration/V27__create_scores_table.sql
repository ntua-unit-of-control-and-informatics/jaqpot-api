ALTER TABLE metrics
    DROP CONSTRAINT fk_metrics_on_model;

CREATE TABLE scores
(
    id                             BIGSERIAL                NOT NULL,
    created_at                     TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at                     TIMESTAMP WITH TIME ZONE NOT NULL,
    model_id                       BIGINT                   NOT NULL,
    score_type                     VARCHAR(255)             NOT NULL,
    r2                             FLOAT,
    mae                            FLOAT,
    rmse                           FLOAT,
    r_squared_diffrzero            FLOAT,
    r_squared_diffrzero_hat        FLOAT,
    abs_diffrzero_hat              FLOAT,
    k                              FLOAT,
    k_hat                          FLOAT,
    accuracy                       FLOAT,
    balanced_accuracy              FLOAT,
    precision                      FLOAT8[],
    recall                         FLOAT8[],
    f1score                        FLOAT8[],
    jaccard                        FLOAT8[],
    matthews_corr_coef             FLOAT,
    confusion_matrix               JSONB,
    multi_class_accuracy           FLOAT,
    multi_class_balanced_accuracy  FLOAT,
    multi_class_precision          FLOAT8[],
    multi_class_recall             FLOAT8[],
    multi_classf1score             FLOAT8[],
    multi_class_jaccard            FLOAT8[],
    multi_class_matthews_corr_coef FLOAT,
    multi_class_confusion_matrix   JSONB,
    CONSTRAINT pk_scores PRIMARY KEY (id)
);

ALTER TABLE scores
    ADD CONSTRAINT FK_SCORES_ON_MODEL FOREIGN KEY (model_id) REFERENCES model (id);

DROP TABLE metrics;
