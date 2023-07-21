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
public class ClubIdAndCategoryResponse {
    private Long clubId;
    private String clubName;
    private Campus campus;
    private ClubType clubType;
    private String belongs;
    private String briefDescription;

    public ClubIdAndCategoryResponse(Club clubAfterUpdate) {
        this.clubId = clubAfterUpdate.getId();
        this.clubName = clubAfterUpdate.getName();
        this.campus = clubAfterUpdate.getCampus();
        this.clubType = clubAfterUpdate.getClubType();
        this.belongs = clubAfterUpdate.getBelongs();
        this.briefDescription = clubAfterUpdate.getBriefActivityDescription();
    }
}
