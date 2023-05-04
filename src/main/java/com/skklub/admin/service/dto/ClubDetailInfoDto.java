package com.skklub.admin.service.dto;

import com.skklub.admin.domain.*;
import com.skklub.admin.domain.enums.Campus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class ClubDetailInfoDto {
    private Long id;

    //============CLUB==============//
    //분류
    private Campus campus;
    private String clubType;
    private String college;
    private String activityType;
    private String briefActivityDescription;

    //Outlines
    private String name;
    private String headLine;
    private String establishAt;
    private String roomLocation;
    private Integer memberAmount;
    private String regularMeetingTime;
    private String mandatoryActivatePeriod;

    //Details
    private String clubDescription;
    private String activityDescription;

    //Files
    private FileNames logo;
    private List<FileNames> activityImages = new ArrayList<>();
    private String webLink1;
    private String webLink2;

    //============RECRUIT==============//
    //모집 시기
    private LocalDateTime recruitStartAt;
    private LocalDateTime recruitEndAt;
    //정원
    private String recruitQuota;
    //디테일
    private String recruitProcessDescription;
    //모집관련 연락처
    private String recruitContact;
    private String recruitWebLink;


    //============PRESIDENT==============//
    private String presidentName;
    private String presidentContact;

    public ClubDetailInfoDto(Club club, Logo logo, List<ActivityImage> activityImage, Recruit recruit, User user) {
        this.id = club.getId();
        this.campus = club.getCampus();
        this.clubType = club.getClubType().toString();
        this.college = club.getCollege().toString();
        this.activityType = club.getActivityType().toString();
        this.briefActivityDescription = club.getBriefActivityDescription();
        this.name = club.getName();
        this.headLine = club.getHeadLine();
        this.establishAt = club.getEstablishAt();
        this.roomLocation = club.getRoomLocation();
        this.memberAmount = club.getMemberAmount();
        this.regularMeetingTime = club.getRegularMeetingTime();
        this.mandatoryActivatePeriod = club.getMandatoryActivatePeriod();
        this.clubDescription = club.getClubDescription();
        this.activityDescription = club.getActivityDescription();
        this.logo = new FileNames(logo);
        this.activityImages = activityImage.stream()
                .map(FileNames::new)
                .collect(Collectors.toList());
        this.webLink1 = club.getWebLink1();
        this.webLink2 = club.getWebLink2();
        this.recruitStartAt = recruit.getStartAt();
        this.recruitEndAt = recruit.getEndAt();
        this.recruitQuota = recruit.getQuota();
        this.recruitProcessDescription = recruit.getProcessDescription();
        this.recruitContact = recruit.getContact();
        this.recruitWebLink = recruit.getWebLink();
        this.presidentName = user.getName();
        this.presidentContact = user.getContact();
    }
}
