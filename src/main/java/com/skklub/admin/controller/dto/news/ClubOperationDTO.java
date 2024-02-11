package com.skklub.admin.controller.dto.news;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClubOperationDTO {
    private String headLine;
    private String mandatoryActivityPeriod;
    private Integer memberAmount;
    private String regularMeetingTime;
    private String roomLocation;
}
