ALTER TABLE model
    ADD COLUMN textsearchable_index_col tsvector
        GENERATED ALWAYS AS (to_tsvector('english', coalesce(name, '') || ' ' || coalesce(description, '') || ' ' ||
                                                    coalesce(tags, ''))) STORED;

CREATE INDEX textsearch_idx ON model USING GIN (textsearchable_index_col);

CREATE INDEX idx_legacy_id ON model (legacy_id);

