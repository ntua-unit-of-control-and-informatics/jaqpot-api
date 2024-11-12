-- Set all confusion matrices to null, as they're being migrated to 2d arrays.
UPDATE scores
SET confusion_matrix             = NULL,
    multi_class_confusion_matrix = NULL
