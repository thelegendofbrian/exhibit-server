## DDL

```sql
create table user(
    id bigint primary key auto_increment,
    name varchar(16) unique key,
    failed_logins tinyint(4),
    salt binary(32),
    salted_hash binary(32)
);

create table quick_auth(
    user_id bigint not null,
    auth_key varchar(36) primary key,
    date_created timestamp default current_timestamp,
    foreign key (user_id) references user(id) on delete cascade
);

create table `group`(
    id bigint primary key auto_increment,
    name varchar(16) not null,
    owner_user_id bigint,
    foreign key (owner_user_id) references user(id) on delete set null
);

create table group_member(
    id bigint primary key auto_increment,
    group_id bigint not null,
    user_id bigint not null,
    unique key(group_id, user_id),
    foreign key (group_id) references `group`(id) on delete cascade,
    foreign key (user_id) references user(id) on delete cascade
);

create table group_member_text(
    id bigint primary key auto_increment,
    group_member_id bigint not null,
    type varchar(16) not null,
    text text,
    unique key(group_member_id, type),
    foreign key (group_member_id) references group_member(id) on delete cascade
);

create table group_member_stats(
    group_member_id bigint primary key,
    points bigint not null default 0,
    streak int not null default 0,
    regular_checkins int not null default 0,
    bonus_checkins int not null default 0,
    missed_checkins int not null default 0,
    foreign key (group_member_id) references group_member(id) on delete cascade
);

create table status(
    id smallint primary key auto_increment,
    description varchar(16) not null
);

create table group_member_stats_state(
    group_member_id bigint primary key,
    last_update date,
    status_id smallint,
    foreign key (group_member_id) references group_member(id) on delete cascade,
    foreign key (status_id) references status(id) on delete restrict
);

create table checkin(
    group_member_id bigint not null,
    date date not null,
    is_bonus char(1) not null,
    primary key (group_member_id, date),
    foreign key (group_member_id) references group_member(id) on delete cascade
);

create table day_of_week(
    id tinyint(4) primary key,
    day varchar(16) not null
);

create table schedule_type(
    id tinyint(4) primary key auto_increment,
    name varchar(16) not null
);

create table schedule(
    id bigint primary key auto_increment,
    group_member_id bigint unique key,
    schedule_type_id tinyint(4) not null,
    start_date date not null,
    foreign key (schedule_type_id) references schedule_type(id) on delete restrict,
    foreign key (group_member_id) references group_member(id) on delete cascade
);

create table schedule_weekly(
    schedule_id bigint not null,
    day_of_week_id tinyint(4) not null,
    primary key (schedule_id, day_of_week_id),
    foreign key (schedule_id) references schedule(id) on delete cascade,
    foreign key (day_of_week_id) references day_of_week(id) on delete restrict
);

create table schedule_interval(
    schedule_id bigint primary key,
    interval_days tinyint(4) not null,
    foreign key (schedule_id) references schedule(id) on delete restrict
);

create table user_settings(
    user_id bigint primary key,
    timezone varchar(32),
    default_group_id bigint,
    display_name varchar(16),
    start_of_week tinyint(4) not null default 1,
    foreign key (user_id) references user(id) on delete cascade,
    foreign key (default_group_id) references `group`(id) on delete set null,
    foreign key (start_of_week) references day_of_week(id) on delete restrict
);

create table statistic_view(
    id tinyint(4) primary key auto_increment,
    name varchar(16) not null
);

create table statistic(
    id int auto_increment,
    view_id tinyint(4),
    name varchar(16) not null,
    primary key(id, view_id),
    foreign key (view_id) references statistic_view(id) on delete restrict
);

create table member_settings_view(
    group_member_id bigint,
    view_id tinyint(4),
    stat_id int,
    primary key(group_member_id, view_id, stat_id),
    foreign key (group_member_id) references group_member(id) on delete cascade,
    foreign key (view_id) references statistic_view(id) on delete restrict,
    foreign key (stat_id) references statistic(id) on delete restrict
);

insert into status(description) values
('In Progress'),
('Ready');

insert into day_of_week(id, day) values
(1, 'Monday'),
(2, 'Tuesday'),
(3, 'Wednesday'),
(4, 'Thursday'),
(5, 'Friday'),
(6, 'Saturday'),
(7, 'Sunday');

insert into schedule_type(name) values
('Weekly'),
('Interval'),
('None');

insert into statistic_view(name) values
('User'),
('Group');

insert into statistic(name, view_id) values
('dayStreak', (select id from statistic_view where name = 'User')),
('adherence', (select id from statistic_view where name = 'User')),
('points', (select id from statistic_view where name = 'User')),
('bonusCheckins', (select id from statistic_view where name = 'User')),
('totalCheckins', (select id from statistic_view where name = 'User')),
('dayStreak', (select id from statistic_view where name = 'Group')),
('adherence', (select id from statistic_view where name = 'Group')),
('points', (select id from statistic_view where name = 'Group')),
('bonusCheckins', (select id from statistic_view where name = 'Group')),
('totalCheckins', (select id from statistic_view where name = 'Group'));
```