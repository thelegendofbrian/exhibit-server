## DDL

```sql
create table exhibit.user(user_name varchar(16) primary key, failed_logins tinyint(4), salt binary(32), salted_hash binary(32));
create table exhibit.quick_auth(user_name varchar(16), auth_key varchar(36), date_created timestamp default current_timestamp);
create table exhibit.checkin(user_name varchar(16), group_name varchar(16), date date, primary key (user_name, group_name, date));
create table exhibit.day_of_week(id tinyint(4) primary key auto_increment, day varchar(16));
create table exhibit.schedule_type(id tinyint(4) primary key auto_increment, name varchar(16));
create table exhibit.schedule(id bigint primary key auto_increment, user_name varchar(16), group_name varchar(16), schedule_type_id tinyint(4), unique key user(user_name, group_name), foreign key(schedule_type_id) references schedule_type(id) on delete restrict);
create table exhibit.schedule_weekly(id bigint primary key,  day_of_week_id tinyint(4), foreign key (id) references schedule(id) on delete cascade, foreign key(day_of_week_id) references day_of_week(id) on delete restrict);
create table exhibit.schedule_interval(id bigint primary key, interval_days tinyint(4), foreign key (id) references schedule(id) on delete cascade);

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