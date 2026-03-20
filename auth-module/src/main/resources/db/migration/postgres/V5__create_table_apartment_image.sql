create table if not exists apartment_image (
    id bigint primary key,
    original_name varchar,
        image text,
        size bigint,
    apartment_id bigint,           -- Внешний ключ на таблицу apartment
    CONSTRAINT fk_apartment
    FOREIGN KEY (apartment_id)
    REFERENCES apartment_info(id)
    ON DELETE CASCADE           -- Удаление фотографий при удалении aппартаментов

);
-- CREATE TABLE IF NOT EXISTS user_photos (
--                                            id SERIAL PRIMARY KEY,          -- Уникальный идентификатор фотографии
--                                            photo_url VARCHAR(255) NOT NULL, -- Ссылка на фотографию
--                                            user_id INT NOT NULL,           -- Внешний ключ на таблицу users
--                                            CONSTRAINT fk_user
--                                                FOREIGN KEY (user_id)
--                                                    REFERENCES users(id)
--                                                    ON DELETE CASCADE           -- Удаление фотографий при удалении пользователя
-- );
create sequence apartment_image_sequence start 2 increment 1;
insert into apartment_image(id, original_name, image, size,apartment_id)
values ('1','test','kygfkyfkf',1,1)