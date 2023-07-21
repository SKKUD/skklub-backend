package com.skklub.admin.controller.dto;

import com.skklub.admin.domain.Club;
import com.skklub.admin.domain.enums.Campus;
import com.skklub.admin.domain.enums.ClubType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClubCreateResponse {
    private Long clubId;
    private String clubName;
    private Campus campus;
    private ClubType clubType;
    private String belongs;
    private String briefActivityDescription;

    private String username;
    private String presidentName;

    public ClubCreateResponse(Club club) {
        this.clubId = club.getId();
        this.clubName = club.getName();
        this.campus = club.getCampus();
        this.clubType = club.getClubType();
        this.belongs = club.getBelongs();
        this.briefActivityDescription = club.getBriefActivityDescription();
        this.username = club.getPresident().getUsername();
        this.presidentName = club.getPresident().getName();
    }
}
