-- Add environment so the same feature_key can have different config per env (dev/staging/prod).
-- Existing rows get environment 'DEV' so current data stays valid.
ALTER TABLE feature_flags
    ADD COLUMN environment VARCHAR(50) NOT NULL DEFAULT 'DEV';

ALTER TABLE feature_flags
    DROP CONSTRAINT uk_feature_key;

ALTER TABLE feature_flags
    ADD CONSTRAINT uk_feature_key_environment UNIQUE (feature_key, environment);
