-- H2 / связка комментариев и квартир

CREATE TABLE IF NOT EXISTS apartment_comment (
                                                 apartment_id BIGINT NOT NULL,
                                                 comment_id BIGINT NOT NULL,
                                                 CONSTRAINT fk_ac_apartment FOREIGN KEY (apartment_id) REFERENCES apartment_info(id) ON DELETE CASCADE,
                                                 CONSTRAINT fk_ac_comment FOREIGN KEY (comment_id) REFERENCES user_comment(id) ON DELETE CASCADE,
                                                 CONSTRAINT pk_apartment_comment PRIMARY KEY (apartment_id, comment_id)
);

-- Индексы для ускорения запросов
CREATE INDEX IF NOT EXISTS idx_apartment_comments_apartment ON apartment_comment(apartment_id);
CREATE INDEX IF NOT EXISTS idx_apartment_comments_comment ON apartment_comment(comment_id);
