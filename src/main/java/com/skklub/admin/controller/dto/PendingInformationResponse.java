package com.skklub.admin.controller.dto;

import com.skklub.admin.domain.PendingClub;
import com.skklub.admin.domain.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingInformationResponse {
    private Long pendingClubId;
    private Role requestTo;

    private String clubName;
    private String briefActivityDescription;
    private String activityDescription;
    private String clubDescription;

    private String presidentName;
    private String presidentContact;

    public PendingInformationResponse(PendingClub pendingClub) {
        this.pendingClubId = pendingClub.getId();
        this.requestTo = pendingClub.getRequestTo();
        this.clubName = pendingClub.getClubName();
        this.briefActivityDescription = pendingClub.getBriefActivityDescription();
        this.activityDescription = pendingClub.getActivityDescription();
        this.clubDescription = pendingClub.getClubDescription();
        this.presidentName = pendingClub.getPresidentName();
        this.presidentContact = pendingClub.getPresidentContact();
    }
}
