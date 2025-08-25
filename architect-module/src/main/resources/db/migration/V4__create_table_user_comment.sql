create table if not exists user_comment(
    id bigint primary key,
    comment varchar,
    grade integer,
    user_id bigint, constraint fk_user foreign key (user_id)
                                       references user_info(id) ON DELETE CASCADE
);

create sequence user_comment_sequence start 2 increment 1;

    insert into user_comment(id,comment,grade)
    values (1,'тестовый коментарий',5)
