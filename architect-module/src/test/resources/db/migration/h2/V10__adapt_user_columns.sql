-- H2 / упрощённые ALTER-операции

-- В H2 sequence не создавались (используем IDENTITY), поэтому DROP SEQUENCE убираем.

ALTER TABLE user_comment
    ALTER COLUMN comment VARCHAR(2000);

ALTER TABLE user_info
    ALTER COLUMN login VARCHAR(255);

ALTER TABLE user_info
    ALTER COLUMN password VARCHAR(255);

ALTER TABLE user_info
    ALTER COLUMN token VARCHAR(255);

ALTER TABLE user_info
    ALTER COLUMN username VARCHAR(255);
