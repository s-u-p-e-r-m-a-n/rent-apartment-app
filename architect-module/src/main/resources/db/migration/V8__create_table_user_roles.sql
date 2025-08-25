create table if not exists user_roles(
    user_id bigint references user_info(id) on delete cascade,
    roles varchar(20) not null,
    primary key (user_id,roles) ---Этот первичный ключ указывает, что комбинация user_id и role должна быть уникальной.
                               ---Это предотвращает дублирование записей (например, один и тот же пользователь не может иметь две одинаковые роли).
);