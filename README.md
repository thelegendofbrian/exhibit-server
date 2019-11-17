## DDL

```sql
create table exhibit.user(
    id bigint primary key auto_increment,
    name varchar(16) unique key,
    failed_logins tinyint(4),
    salt binary(32),
    salted_hash binary(32)
);

create table exhibit.quick_auth(
    user_id bigint not null,
    auth_key varchar(36) primary key,
    date_created timestamp default current_timestamp,
    foreign key (user_id) references user(id) on delete cascade
);

create table exhibit.`group`(
    id bigint primary key auto_increment,
    name varchar(16) not null
);

create table exhibit.group_member(
    id bigint primary key auto_increment,
    group_id bigint not null,
    user_id bigint not null,
    foreign key (group_id) references `group`(id) on delete cascade,
    foreign key (user_id) references user(id) on delete cascade
);

create table exhibit.checkin(
    user_id bigint not null,
    group_id bigint not null,
    date date not null,
    primary key (user_id, group_id, date),
    foreign key (user_id) references user(id) on delete cascade,
    foreign key (group_id) references `group`(id) on delete cascade
);

create table exhibit.day_of_week(
    id tinyint(4) primary key auto_increment,
    day varchar(16) not null
);

create table exhibit.schedule_type(
    id tinyint(4) primary key auto_increment,
    name varchar(16) not null
);

create table exhibit.schedule(
    id bigint primary key auto_increment,
    user_id bigint not null,
    group_id bigint not null,
    schedule_type_id tinyint(4) not null,
    unique key user(user_id, group_id),
    foreign key (schedule_type_id) references schedule_type(id) on delete cascade,
    foreign key (user_id) references user(id) on delete cascade,
    foreign key (group_id) references `group`(id) on delete cascade
);

create table exhibit.schedule_weekly(
    schedule_id bigint primary key,
    day_of_week_id tinyint(4) not null,
    foreign key (schedule_id) references schedule(id) on delete cascade,
    foreign key (day_of_week_id) references day_of_week(id) on delete cascade
);

create table exhibit.schedule_interval(
    schedule_id bigint primary key,
    interval_days tinyint(4) not null,
    foreign key (schedule_id) references schedule(id) on delete cascade
);

insert into exhibit.day_of_week(day) values
('Monday'),
('Tuesday'),
('Wednesday'),
('Thursday'),
('Friday'),
('Saturday'),
('Sunday');

insert into exhibit.schedule_type(name) values
('Weekly'),
('Interval');
```