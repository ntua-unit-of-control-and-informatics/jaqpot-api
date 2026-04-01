-- Rename columns to match Hibernate 7 (Spring Boot 4) naming strategy
-- Hibernate 7 now inserts underscore before uppercase letters that follow a digit
-- f1Score -> f1_score (was f1score in Hibernate 6)
-- multiClassF1Score -> multi_class_f1_score (was multi_classf1score in Hibernate 6)
ALTER TABLE scores RENAME COLUMN f1score TO f1_score;
ALTER TABLE scores RENAME COLUMN multi_classf1score TO multi_class_f1_score;
