package com.skklub.admin.controller.dto;

import com.skklub.admin.domain.Club;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClubUpdateRequest {
    @NotBlank
    private String clubName;
    @NotBlank
    private String activityDescription;
    @NotBlank
    private String briefActivityDescription;
    @NotBlank
    private String clubDescription;

    //Can be NULL;
    @DecimalMin(value = "1397")
    private Integer establishDate;
    private String headLine;
    private String mandatoryActivatePeriod;
    @DecimalMin(value = "0")
    private Integer memberAmount;
    private String regularMeetingTime;
    private String roomLocation;
    private String webLink1;
    private String webLink2;

    public Club toEntity() {
        return new Club(clubName, activityDescription, briefActivityDescription,
                clubDescription,  establishDate, headLine,
                mandatoryActivatePeriod, memberAmount, regularMeetingTime, roomLocation, webLink1, webLink2);
    }
}
