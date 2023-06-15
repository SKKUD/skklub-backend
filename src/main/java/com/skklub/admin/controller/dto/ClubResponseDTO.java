package com.skklub.admin.controller.dto;

import com.skklub.admin.domain.enums.Campus;
import com.skklub.admin.service.dto.ClubDetailInfoDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClubResponseDTO {
    private Long id;

    //============CLUB==============//
    //분류
    private Campus campus;
    private String clubType;
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

    private String webLink1;
    private String webLink2;

    //============RECRUIT==============//
    private Optional<RecruitDto> recruit;

    //============PRESIDENT==============//
    private String presidentName;
    private String presidentContact;

    //Files
    private S3DownloadDto logo;
    private List<S3DownloadDto> activityImages = new ArrayList<>();

    public ClubResponseDTO(ClubDetailInfoDto clubDetailInfoDto, S3DownloadDto logo, List<S3DownloadDto> activityImages) {
        this.id = clubDetailInfoDto.getId();
        this.campus = clubDetailInfoDto.getCampus();
        this.clubType = clubDetailInfoDto.getClubType();
        this.belongs = clubDetailInfoDto.getBelongs();
        this.briefActivityDescription = clubDetailInfoDto.getBriefActivityDescription();
        this.name = clubDetailInfoDto.getName();
        this.headLine = clubDetailInfoDto.getHeadLine();
        this.establishAt = clubDetailInfoDto.getEstablishAt();
        this.roomLocation = clubDetailInfoDto.getRoomLocation();
        this.memberAmount = clubDetailInfoDto.getMemberAmount();
        this.regularMeetingTime = clubDetailInfoDto.getRegularMeetingTime();
        this.mandatoryActivatePeriod = clubDetailInfoDto.getMandatoryActivatePeriod();
        this.clubDescription = clubDetailInfoDto.getClubDescription();
        this.activityDescription = clubDetailInfoDto.getActivityDescription();
        this.logo = logo;
        this.activityImages = activityImages;
        this.webLink1 = clubDetailInfoDto.getWebLink1();
        this.webLink2 = clubDetailInfoDto.getWebLink2();
        this.recruit = clubDetailInfoDto.getRecruit();
        this.presidentName = clubDetailInfoDto.getPresidentName();
        this.presidentContact = clubDetailInfoDto.getPresidentContact();
    }
}
