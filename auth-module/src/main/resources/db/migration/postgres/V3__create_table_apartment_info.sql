create table if not exists apartment_info(
    id           bigint primary key,
    availability varchar,
    price        varchar,
    room_count   varchar,
    address_id bigint references address_info(id),
    average_rating double precision,
    user_id bigint,  constraint pk_user foreign key (user_id) references user_info(id)
);
create sequence apartment_info_sequence start 2 increment 1;

insert into apartment_info(id,availability,price,room_count,address_id,average_rating)
values (1,'все хорошо','2','45',1,4.5)


