ALTER TABLE scores
    ADD folds INTEGER;

ALTER TABLE scores
    DROP COLUMN abs_diffrzero_hat;

ALTER TABLE scores
    DROP COLUMN k;

ALTER TABLE scores
    DROP COLUMN k_hat;

ALTER TABLE scores
    DROP COLUMN r_squared_diffrzero;

ALTER TABLE scores
    DROP COLUMN r_squared_diffrzero_hat;
