DROP TABLE IF EXISTS `user`;
create table user
(
    id         int auto_increment
        primary key,
    username   varchar(32) not null,
    password   varchar(64) null,
    wechat_id  varchar(64) not null,
    email      varchar(48) null,
    join_time  datetime    null,
    quit_time  datetime    null,
    is_admin   tinyint(1)  null,
    has_quited tinyint(1)  null,
    constraint User_pk2
        unique (id),
    constraint User_pk3
        unique (wechat_id)
);

DROP TABLE IF EXISTS `record`;
create table record
(
    id      int auto_increment
        primary key,
    user_id int          not null,
    time    datetime     null,
    url     varchar(128) not null
);

create index record_time_index
    on record (time);