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
    name varchar(16) not null,
    owner_user_id bigint,
    foreign key (owner_user_id) references user(id) on delete set null
);

create table exhibit.group_member(
    id bigint primary key auto_increment,
    group_id bigint not null,
    user_id bigint not null,
    unique key(group_id, user_id),
    foreign key (group_id) references `group`(id) on delete cascade,
    foreign key (user_id) references user(id) on delete cascade
);

create table exhibit.group_member_stats(
    group_member_id bigint primary key,
    points bigint not null default 0,
    streak int not null default 0,
    regular_checkins int not null default 0,
    bonus_checkins int not null default 0,
    missed_checkins int not null default 0,
    foreign key (group_member_id) references group_member(id) on delete cascade
);

create table exhibit.checkin(
    group_member_id bigint not null,
    date date not null,
    is_bonus char(1) not null,
    primary key (group_member_id, date),
    foreign key (group_member_id) references group_member(id) on delete cascade
);

create table exhibit.day_of_week(
    id tinyint(4) primary key,
    day varchar(16) not null
);

create table exhibit.schedule_type(
    id tinyint(4) primary key auto_increment,
    name varchar(16) not null
);

create table exhibit.schedule(
    id bigint primary key auto_increment,
    group_member_id bigint unique key,
    schedule_type_id tinyint(4) not null,
    start_date date not null,
    foreign key (schedule_type_id) references schedule_type(id) on delete restrict,
    foreign key (group_member_id) references group_member(id) on delete cascade
);

create table exhibit.schedule_weekly(
    schedule_id bigint primary key,
    day_of_week_id tinyint(4) not null,
    foreign key (schedule_id) references schedule(id) on delete cascade,
    foreign key (day_of_week_id) references day_of_week(id) on delete restrict
);

create table exhibit.schedule_interval(
    schedule_id bigint primary key,
    interval_days tinyint(4) not null,
    foreign key (schedule_id) references schedule(id) on delete cascade
);

create table exhibit.user_settings(
    user_id bigint primary key,
    timezone varchar(16),
    default_group_id bigint,
    display_name varchar(16),
    foreign key (user_id) references user(id) on delete cascade,
    foreign key (default_group_id) references `group`(id) on delete set null
);

insert into exhibit.day_of_week(id, day) values
(1, 'Monday'),
(2, 'Tuesday'),
(3, 'Wednesday'),
(4, 'Thursday'),
(5, 'Friday'),
(6, 'Saturday'),
(7, 'Sunday');

insert into exhibit.schedule_type(name) values
('Weekly'),
('Interval');
```