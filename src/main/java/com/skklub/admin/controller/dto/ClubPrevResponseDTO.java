package com.skklub.admin.controller.dto;

import com.skklub.admin.domain.Club;
import com.skklub.admin.domain.enums.Campus;
import com.skklub.admin.domain.enums.ClubType;
import com.skklub.admin.service.dto.ClubPrevDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClubPrevResponseDTO {
    private Long id;
    private String name;
    private Campus campus;
    private ClubType clubType;
    private String belongs;
    private String briefActivityDescription;
    private S3DownloadDto logo;

    public ClubPrevResponseDTO(Club club, S3DownloadDto logo) {
        this.id = club.getId();
        this.name = club.getName();
        this.campus = club.getCampus();
        this.clubType = club.getClubType();
        this.belongs = club.getBelongs();
        this.briefActivityDescription = club.getBriefActivityDescription();
        this.logo = logo;
    }
}
