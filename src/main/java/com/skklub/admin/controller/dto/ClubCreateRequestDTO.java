package com.skklub.admin.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.skklub.admin.domain.Club;
import com.skklub.admin.domain.enums.ActivityType;
import com.skklub.admin.domain.enums.Campus;
import com.skklub.admin.domain.enums.ClubType;
import com.skklub.admin.domain.enums.College;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClubCreateRequestDTO {
    private String clubName;
    private String activityDescription;
    private String briefActivityDescription;
    private String clubDescription;

    //enums
    private String activityType;
    private String campus;
    private String college;
    private String clubType;

    //Can be NULL;
    private String establishDate;
    private String headLine;
    private String mandatoryActivatePeriod;
    private Integer memberAmount;
    private String regularMeetingTime;
    private String roomLocation;
    private String webLink1;
    private String webLink2;

    public Club toEntity() {
        return new Club(clubName, activityDescription, ActivityType.valueOf(activityType), ClubType.valueOf(clubType), briefActivityDescription, Campus.valueOf(campus), clubDescription, College.valueOf(college), establishDate, headLine, mandatoryActivatePeriod, memberAmount, regularMeetingTime, roomLocation, webLink1, webLink2);
    }
}
