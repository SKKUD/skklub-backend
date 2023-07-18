drop trigger if exists toD$$
drop trigger if exists toC$$

alter table notice modify column created_at datetime(6) default now()$$

create trigger toD
    after delete on club
    for each row
begin
    insert into deleted_club(
        club_id, created_at, last_modified_at, created_by, last_modified_by, activity_description, belongs, brief_activity_description, campus, club_description, club_type, establish_at, head_line, logo_id, mandatory_activate_period, member_amount, name, recruit_id, regular_meeting_time, room_location, user_id, web_link1, web_link2
    ) values (OLD.club_id, OLD.created_at, OLD.last_modified_at, OLD.created_by, OLD.last_modified_by, OLD.activity_description, OLD.belongs, OLD.brief_activity_description, OLD.campus, OLD.club_description, OLD.club_type, OLD.establish_at, OLD.head_line, OLD.logo_id, OLD.mandatory_activate_period, OLD.member_amount, OLD.name, NULL, OLD.regular_meeting_time, OLD.room_location, OLD.user_id, OLD.web_link1, OLD.web_link2)
    ;
    end$$

create trigger toC
    after delete on deleted_club
    for each row
begin
    insert into club(
        club_id, created_at, last_modified_at, created_by, last_modified_by, activity_description, belongs, brief_activity_description, campus, club_description, club_type, establish_at, head_line, logo_id, mandatory_activate_period, member_amount, name, recruit_id, regular_meeting_time, room_location, user_id, web_link1, web_link2
    ) values (OLD.club_id, OLD.created_at, OLD.last_modified_at, OLD.created_by, OLD.last_modified_by, OLD.activity_description, OLD.belongs, OLD.brief_activity_description, OLD.campus, OLD.club_description, OLD.club_type, OLD.establish_at, OLD.head_line, OLD.logo_id, OLD.mandatory_activate_period, OLD.member_amount, OLD.name, NULL, OLD.regular_meeting_time, OLD.room_location, OLD.user_id, OLD.web_link1, OLD.web_link2)
    ;
    end$$