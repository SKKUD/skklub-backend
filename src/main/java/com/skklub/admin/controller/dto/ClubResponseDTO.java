package com.skklub.admin.controller.dto;

import com.skklub.admin.domain.*;
import com.skklub.admin.domain.enums.ActivityType;
import com.skklub.admin.domain.enums.Campus;
import com.skklub.admin.domain.enums.ClubType;
import com.skklub.admin.domain.enums.College;
import com.skklub.admin.service.dto.ClubDetailInfoDto;
import com.skklub.admin.service.dto.FileNames;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClubResponseDTO {
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

    //Files
    private S3DownloadDto logo;
    private List<S3DownloadDto> activityImages = new ArrayList<>();

    public ClubResponseDTO(ClubDetailInfoDto clubDetailInfoDto, S3DownloadDto logo, List<S3DownloadDto> activityImages) {
        this.id = clubDetailInfoDto.getId();
        this.campus = clubDetailInfoDto.getCampus();
        this.clubType = clubDetailInfoDto.getClubType().toString();
        this.college = clubDetailInfoDto.getCollege().toString();
        this.activityType = clubDetailInfoDto.getActivityType().toString();
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
        this.recruitStartAt = clubDetailInfoDto.getRecruitStartAt();
        this.recruitEndAt = clubDetailInfoDto.getRecruitEndAt();
        this.recruitQuota = clubDetailInfoDto.getRecruitQuota();
        this.recruitProcessDescription = clubDetailInfoDto.getRecruitProcessDescription();
        this.recruitContact = clubDetailInfoDto.getRecruitContact();
        this.recruitWebLink = clubDetailInfoDto.getRecruitWebLink();
        this.presidentName = clubDetailInfoDto.getPresidentName();
        this.presidentContact = clubDetailInfoDto.getPresidentContact();
    }
}
