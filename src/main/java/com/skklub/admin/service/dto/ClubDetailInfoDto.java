package com.skklub.admin.service.dto;

import com.skklub.admin.controller.dto.RecruitDto;
import com.skklub.admin.domain.*;
import com.skklub.admin.domain.enums.Campus;
import com.skklub.admin.domain.enums.ClubType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class ClubDetailInfoDto {
    private Long id;

    //============CLUB==============//
    //분류
    private Campus campus;
    private ClubType clubType;
    private String belongs;
    private String briefActivityDescription;

    //Outlines
    private String name;
    private String headLine;
    private Integer establishAt;
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
    private Optional<RecruitDto> recruit;


    //============PRESIDENT==============//
    private String presidentName;
    private String presidentContact;

    public ClubDetailInfoDto(Club club) {
        Logo logo = club.getLogo();
        List<ActivityImage> activityImages = club.getActivityImages();
        Optional<Recruit> recruit = Optional.ofNullable(club.getRecruit());
        User user = club.getPresident();
        this.id = club.getId();
        this.campus = club.getCampus();
        this.clubType = club.getClubType();
        this.belongs = club.getBelongs();
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
        this.activityImages = activityImages.stream()
                .map(FileNames::new)
                .collect(Collectors.toList());
        this.webLink1 = club.getWebLink1();
        this.webLink2 = club.getWebLink2();
        this.recruit = recruit.map(RecruitDto::new);
        this.presidentName = user.getName();
        this.presidentContact = user.getContact();
    }
}
