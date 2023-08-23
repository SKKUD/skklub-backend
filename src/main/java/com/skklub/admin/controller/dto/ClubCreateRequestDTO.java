package com.skklub.admin.controller.dto;

import com.skklub.admin.domain.Club;
import com.skklub.admin.domain.enums.Campus;
import com.skklub.admin.domain.enums.ClubType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClubCreateRequestDTO {
    @NotBlank
    private String clubName;
    @NotBlank
    private String activityDescription;
    @NotBlank
    private String briefActivityDescription;
    @NotBlank
    private String clubDescription;

    //enums
    @NotBlank
    private String belongs;
    @NotNull
    private Campus campus;
    @NotNull
    private ClubType clubType;

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
        return new Club(clubName, activityDescription, belongs,
                clubType, briefActivityDescription, campus,
                clubDescription,  establishDate, headLine,
                mandatoryActivatePeriod, memberAmount, regularMeetingTime, roomLocation, webLink1, webLink2);
    }
}
