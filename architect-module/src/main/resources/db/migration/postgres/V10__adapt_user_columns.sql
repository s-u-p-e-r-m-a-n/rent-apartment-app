DROP SEQUENCE address_info_sequence CASCADE;

DROP SEQUENCE apartment_image_sequence CASCADE;

DROP SEQUENCE apartment_info_sequence CASCADE;

ALTER TABLE user_comment
    ALTER COLUMN comment TYPE VARCHAR(2000) USING (comment::VARCHAR(2000));

ALTER TABLE user_info
    ALTER COLUMN login TYPE VARCHAR(255) USING (login::VARCHAR(255));

ALTER TABLE user_info
    ALTER COLUMN password TYPE VARCHAR(255) USING (password::VARCHAR(255));

ALTER TABLE user_info
    ALTER COLUMN token TYPE VARCHAR(255) USING (token::VARCHAR(255));

ALTER TABLE user_info
    ALTER COLUMN username TYPE VARCHAR(255) USING (username::VARCHAR(255));