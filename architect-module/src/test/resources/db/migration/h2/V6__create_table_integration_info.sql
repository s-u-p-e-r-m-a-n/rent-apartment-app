-- H2 / простая таблица для интеграций

CREATE TABLE IF NOT EXISTS integration_info (
                                                id VARCHAR(64) PRIMARY KEY,
                                                path VARCHAR(1024),
                                                token VARCHAR(512),
                                                description VARCHAR(512)
);

INSERT INTO integration_info (id, path, token, description)
VALUES (
           'GEO',
           'https://api.opencagedata.com/geocode/v1/json?q=%s+%s&key=%s',
           'YTE3M2QyYjgzNTBkNGIxODg5MmYxNTIxZDI5ZDBmNWE=',
           'сервис предоставления информации по геолокации'
       );
