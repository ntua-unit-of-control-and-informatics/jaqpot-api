UPDATE feature
SET possible_values = (REPLACE(possible_values::TEXT, '"value"', '"description"'))::JSONB
WHERE possible_values IS NOT NULL;

UPDATE feature
SET possible_values = (REPLACE(possible_values::TEXT, '"key"', '"value"'))::JSONB
WHERE possible_values IS NOT NULL;
