package com.skklub.admin.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.skklub.admin.domain.PendingClub;
import com.skklub.admin.domain.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingInformationResponse {
    private Long pendingClubId;
    private Role requestTo;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime requestedAt;

    private String clubName;
    private String briefActivityDescription;
    private String activityDescription;
    private String clubDescription;

    private String presidentName;
    private String presidentContact;

    public PendingInformationResponse(PendingClub pendingClub) {
        this.pendingClubId = pendingClub.getId();
        this.requestTo = pendingClub.getRequestTo();
        this.requestedAt = pendingClub.getCreatedAt().truncatedTo(ChronoUnit.MINUTES);
        this.clubName = pendingClub.getClubName();
        this.briefActivityDescription = pendingClub.getBriefActivityDescription();
        this.activityDescription = pendingClub.getActivityDescription();
        this.clubDescription = pendingClub.getClubDescription();
        this.presidentName = pendingClub.getPresidentName();
        this.presidentContact = pendingClub.getPresidentContact();
    }
}
