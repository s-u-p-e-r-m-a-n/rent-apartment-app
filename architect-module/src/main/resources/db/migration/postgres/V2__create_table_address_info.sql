create table if not exists address_info(
        id bigint primary key,
        city varchar,
        house_number varchar,
        street varchar
);

create sequence address_info_sequence start 2 increment 1;

insert into address_info (id,city,house_number,street)

values (1,'Elec',23,'Pushkina')