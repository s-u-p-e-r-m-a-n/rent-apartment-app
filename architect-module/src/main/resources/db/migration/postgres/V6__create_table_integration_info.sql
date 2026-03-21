create table if not exists integration_info(
    id varchar primary key,
    path varchar,
    token varchar,
    description varchar
);
insert into integration_info (id,path,token,description)
values('GEO',
       'https://api.opencagedata.com/geocode/v1/json?q=%s+%s&key=%s',
       'YTE3M2QyYjgzNTBkNGIxODg5MmYxNTIxZDI5ZDBmNWE=',
       'сервис предоставления информации по геолокации');