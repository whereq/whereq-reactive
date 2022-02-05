create table application_instance(
    id SERIAL PRIMARY KEY, 
    server varchar(200), 
    app varchar(200),
    instance int, 
    uri varchar(200),
    status varchar(10),
    build_version varchar(50),
    credentials varchar(200),
    instance_json varchar(2000),
    status_uri varchar(200),
    status_json varchar(2000)
);

create unique index idx_app on application_instance(server, app, instance);
