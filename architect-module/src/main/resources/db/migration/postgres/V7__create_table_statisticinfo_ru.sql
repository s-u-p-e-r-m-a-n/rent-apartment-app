create table if not exists statistic_info (
    id serial primary key,
    description varchar,
    registration_time varchar
);

--функция
create or replace function save_new_user_info()
returns trigger as $$
begin
    insert into statistic_info(description)
    values ('добавлен новый пользователь '|| new.username || ' почта пользователя ' || new.login );
    return new;
end;
$$ language plpgsql;
--триггер
create trigger save_new_user_info_trigger
    after insert on user_info
    for each row execute function save_new_user_info();--для каждой строки выполнтить функцию

