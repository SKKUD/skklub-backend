drop table if exists recruit;
drop table if exists recruit_status;
drop table if exists extra_file;
drop table if exists activity_image;
drop table if exists club_meta;
drop table if exists club_operation;
drop table if exists logo;
drop table if exists club_categorization;
drop table if exists notice;
drop table if exists user;
drop table if exists thumbnail;
drop table if exists file_name;
drop table if exists request_club_required;
drop table if exists request_club_optional;
drop table if exists request_user;

# 동아리 고정 정보
create table club_meta (
	club_meta_id bigint primary key not null auto_increment,
    
    created_at datetime(6) not null default now(6),
    last_modified_at datetime(6) not null default now(6),
    created_by varchar(20) not null,
    last_modified_by varchar(20) not null,

    name varchar(30) not null,
	activity_type varchar(20) not null,
    description text not null,
    activity_description text not null,
    establish_at int check(establish_at >= 1398 and establish_at <= 2999),

    club_operation_id bigint not null,
    logo_id bigint not null
);

# 동아리 운영 관련 유동 정보
create table club_operation(
    club_operation_id bigint primary key not null auto_increment,

    head_line varchar(50),
    mandatory_activate_period varchar(50),
    member_amount int,
    regular_meeting_time varchar(50),
    room_location varchar(50),

    visibility tinyint not null default 1,
    alive tinyint not null default 1,

    user_id bigint not null,
    club_categorization_id bigint not null
);

create table club_categorization(
	club_categorization_id bigint primary key not null auto_increment,

	campus varchar(2) not null check(campus in ('명륜', '율전')),
	club_type varchar(10) not null check(club_type in ('동아리연합회', '중앙동아리', '준중앙동아리', '기타동아리', '소모임', '학회', '학생단체')),
	belongs varchar(10) not null
);

create table user (
	user_id bigint primary key not null auto_increment,

    created_at datetime(6) not null default now(6),
    last_modified_at datetime(6) not null default now(6),

    name varchar(20) not null,
    contact varchar(50),
    
    username varchar(20) not null,
    password varchar(255) not null,
    role varchar(50) not null default 'ROLE_USER' check(role in ('ROLE_MASTER', 'ROLE_ADMIN_SEOUL_CENTRAL', 'ROLE_ADMIN_SUWON_CENTRAL', 'ROLE_USER'))
);

create table recruit (
	recruit_id bigint primary key not null auto_increment,

    created_at datetime(6) not null default now(6),
    last_modified_at datetime(6) not null default now(6),
    created_by varchar(20) not null,
    last_modified_by varchar(20) not null,

    process_description varchar(255) not null,
    contact varchar(255),
    quota varchar(50) not null,

    club_operation_id bigint not null,
    recruit_status_id bigint not null
);

create table recruit_status (
    recruit_status_id bigint primary key not null auto_increment,

    status varchar(10) not null default '상시모집' check(status in ('모집예정', '모집중', '상시모집', '모집종료')),
    start_at datetime(6) not null,
    end_at datetime(6) not null default '2999-12-31',

    constraint recruit_time_integrity check(start_at <= end_at)
);

create table notice (
	notice_id bigint primary key not null auto_increment,

    created_at datetime(6) not null default now(6),
    last_modified_at datetime(6) not null default now(6),
    created_by varchar(20) not null,
    last_modified_by varchar(20) not null,

    title varchar(50) not null,
    content text not null,

    user_id bigint not null,
    thumbnail_id bigint not null
);


create table file_name (
    file_name_id bigint primary key not null,

    dtype varchar(30) not null,

    created_at datetime(6) not null default now(6),
    last_modified_at datetime(6) not null default now(6),
    
    original_name varchar(255) not null,
    uploaded_name varchar(255) not null
);

create table extra_file (
    file_name_id bigint primary key not null,

    notice_id bigint not null
);

create table thumbnail (
    file_name_id bigint primary key not null
);

create table activity_image (
    file_name_id bigint primary key not null,

    club_meta_id bigint not null
);

create table logo (
    file_name_id bigint primary key not null
);

create table request_club_required (
	request_club_required_id  bigint primary key not null auto_increment,

	name varchar(30) not null,
   	campus varchar(2) not null check(campus in ('명륜', '율전')),
	brief_activity_description varchar(50) not null,

    request_user_id bigint not null,
    request_club_optional_id bigint not null
);

create table request_user (
    request_user_id bigint primary key not null auto_increment,

    username varchar(20) not null,
    password varchar(255) not null,
    name varchar(20) not null,
    contact varchar(11) not null
);

create table request_club_optional (
    request_club_optional_id bigint primary key not null auto_increment,

    description text null,
    activity_type text null,
    establish_at int default null check(establish_at >= 1398 and establish_at <= 2999),
    head_line varchar(50) default null,
    mandatory_activate_period varchar(50),
    member_amount int,
    regular_meeting_time varchar(50),
    room_location varchar(50)
);

alter table club_meta
add constraint FK_club_meta__logo
foreign key (logo_id)
references logo(file_name_id);

alter table club_operation
add constraint FK_club_operation__user
foreign key (user_id)
references user(user_id);

alter table recruit
add constraint FK_recruit__club_operation
foreign key (club_operation_id)
references club_operation(club_operation_id);

alter table club_operation
add constraint FK_club_operation__club_categorization
foreign key (club_categorization_id)
references club_categorization(club_categorization_id);

alter table club_meta
add constraint FK_club_meta__club_operation
foreign key (club_operation_id)
references club_operation (club_operation_id);

alter table notice 
add constraint FK_notice__thumbnail
foreign key (thumbnail_id)
references thumbnail(file_name_id);

alter table notice
add constraint FK_notice__user
foreign key (user_id)
references user(user_id);

alter table extra_file 
add constraint FK_extra_file__notice
foreign key (notice_id)
references notice(notice_id);

alter table activity_image
add constraint FK_activity_image__club_meta
foreign key (club_meta_id)
references club_meta(club_meta_id);

alter table extra_file 
add constraint FK_extra_file__file_name
foreign key (file_name_id)
references file_name(file_name_id);

alter table activity_image 
add constraint FK_activity_image__file_name
foreign key (file_name_id)
references file_name(file_name_id);

alter table thumbnail 
add constraint FK_thumbnail__file_name
foreign key (file_name_id)
references file_name(file_name_id);

alter table logo 
add constraint FK_logo__file_name
foreign key (file_name_id)
references file_name(file_name_id);

alter table recruit
add constraint FK_recruit__recruit_status
foreign key (recruit_status_id)
references recruit_status(recruit_status_id);

alter table request_club_required
add constraint FK_request_club_required__request_club_optional
foreign key (request_club_optional_id)
references request_club_optional(request_club_optional_id);

alter table request_club_required
add constraint FK_request_club_required__request_user
foreign key (request_user_id)
references request_user(request_user_id);

DELIMITER $$
DROP EVENT IF EXISTS update_recruit_status$$
CREATE EVENT IF NOT EXISTS update_recruit_status
ON SCHEDULE EVERY 1 DAY STARTS TIMESTAMP(CURRENT_DATE, '00:00:00') 
DO
BEGIN
    UPDATE recruit_status SET status = CASE
        WHEN now() BETWEEN start_at AND end_at THEN '모집중'
        WHEN now() > end_at THEN '모집종료'
   WHEN now() < start_at THEN '모집예정'
    END;
END$$
DELIMITER ;