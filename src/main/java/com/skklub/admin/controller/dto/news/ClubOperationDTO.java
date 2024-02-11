package com.skklub.admin.controller.dto.news;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Optional;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClubOperationDTO {
    private Long id;
    private String headLine;
    private String mandatoryActivatePeriod;
    private Integer memberAmount;
    private String regularMeetingTime;
    private String roomLocation;

    public Optional<String> getHeadLine() {
        return Optional.ofNullable(headLine);
    }

    public Optional<String> getMandatoryActivatePeriod() {
        return Optional.ofNullable(mandatoryActivatePeriod);
    }

    public Optional<Integer> getMemberAmount() {
        return Optional.ofNullable(memberAmount);
    }

    public Optional<String> getRegularMeetingTime() {
        return Optional.ofNullable(regularMeetingTime);
    }

    public Optional<String> getRoomLocation() {
        return Optional.ofNullable(roomLocation);
    }
}
