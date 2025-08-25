-- H2 / добавление колонки password_hash + индекс

ALTER TABLE user_info
    ADD COLUMN IF NOT EXISTS password_hash VARCHAR(255);

UPDATE user_info
SET password_hash = password
WHERE password_hash IS NULL;

CREATE INDEX IF NOT EXISTS idx_user_info_login ON user_info(login);

-- Примечание: колонку password можно удалить отдельной миграцией
