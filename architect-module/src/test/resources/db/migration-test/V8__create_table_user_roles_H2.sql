-- H2 / таблица связки пользователей и ролей

CREATE TABLE IF NOT EXISTS user_roles (
                                          user_id BIGINT NOT NULL,
                                          roles VARCHAR(20) NOT NULL,
                                          CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES user_info(id) ON DELETE CASCADE,
                                          CONSTRAINT pk_user_roles PRIMARY KEY (user_id, roles)
);
