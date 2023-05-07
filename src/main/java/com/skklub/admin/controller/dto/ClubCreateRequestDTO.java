package com.skklub.admin.controller.dto;

import com.skklub.admin.domain.Club;
import com.skklub.admin.domain.enums.Campus;
import com.skklub.admin.domain.enums.ClubType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClubCreateRequestDTO {
    private String clubName;
    private String activityDescription;
    private String briefActivityDescription;
    private String clubDescription;

    //enums
    private String belongs;
    private Campus campus;
    private ClubType clubType;

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
        return new Club(clubName, activityDescription, belongs,
                clubType, briefActivityDescription, campus,
                clubDescription,  establishDate, headLine,
                mandatoryActivatePeriod, memberAmount, regularMeetingTime, roomLocation, webLink1, webLink2);
    }
}
