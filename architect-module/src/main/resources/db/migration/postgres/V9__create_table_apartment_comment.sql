CREATE TABLE apartment_comment (
apartment_id BIGINT NOT NULL REFERENCES apartment_info(id) ON DELETE CASCADE,
comment_id BIGINT NOT NULL REFERENCES user_comment(id) ON DELETE CASCADE,
PRIMARY KEY (apartment_id, comment_id)
);
-- Индексы для ускорения запросов
CREATE INDEX idx_apartment_comments_apartment ON apartment_comment(apartment_id);
CREATE INDEX idx_apartment_comments_comment ON apartment_comment(comment_id);