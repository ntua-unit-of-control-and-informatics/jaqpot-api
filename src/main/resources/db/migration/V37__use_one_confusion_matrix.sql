-- Start a transaction
BEGIN;

-- Make sure we don't lose data - verify non-null values are copied
SELECT COUNT(*)
FROM scores
WHERE multi_class_confusion_matrix IS NOT NULL;
SELECT COUNT(*)
FROM scores
WHERE confusion_matrix IS NULL
  AND multi_class_confusion_matrix IS NOT NULL;

-- Do the update
UPDATE scores
SET confusion_matrix = multi_class_confusion_matrix
WHERE multi_class_confusion_matrix IS NOT NULL;

-- Verify the update worked
SELECT COUNT(*)
FROM scores
WHERE confusion_matrix IS NULL
  AND multi_class_confusion_matrix IS NOT NULL;

-- If everything looks good, drop the column
ALTER TABLE scores
    DROP COLUMN multi_class_confusion_matrix;

COMMIT;
