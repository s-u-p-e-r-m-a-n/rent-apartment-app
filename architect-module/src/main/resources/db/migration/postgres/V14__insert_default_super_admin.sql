-- 1. Создаём SUPER_ADMIN
INSERT INTO user_info (id, date_registration, login, password_hash, username, verification)
SELECT
    nextval('user_info_sequence'), -- id выдаст sequence, начнёт с 2
    now(),
    'superadmin@mail.com',
    '$2a$10$GIG0LCr4wmI9ENvUzbhCkuBzZzug0BK/68M8kiJ1WJZj9Ju5A76Ya',
    'SUPER_ADMIN',
    'verified'
WHERE NOT EXISTS (
    SELECT 1 FROM user_info WHERE login = 'superadmin@mail.com'
);

-- 2. Назначаем ему роль SUPER_ADMIN
INSERT INTO user_roles (user_id, roles)
SELECT id, 'SUPER_ADMIN'
FROM user_info
WHERE login = 'superadmin@mail.com'
ON CONFLICT (user_id, roles) DO NOTHING;
