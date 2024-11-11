UPDATE scores
SET confusion_matrix = NULL
WHERE confusion_matrix IS NOT NULL;
