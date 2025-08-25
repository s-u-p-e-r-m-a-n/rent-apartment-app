create table if not exists user_info(
    id bigint primary key,
    date_registration timestamp,
    login varchar,
    password varchar,
    token varchar,
    username varchar,
    verification varchar
);

    create sequence user_info_sequence start 2 increment 1;

insert into user_info(id, date_registration, login, password, token, username,verification)
values (1,
        null,
        'test',
        'test',
        '05d64990-c27a-415b-a735-420188f6eeda|[SUPER_ADMIN]|2034-12-03T21:14:05.376243',
        'test','verified')
