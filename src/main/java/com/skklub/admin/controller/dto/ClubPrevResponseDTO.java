package com.skklub.admin.controller.dto;

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
    private String belongs;
    private String briefActivityDescription;
    private S3DownloadDto logo;

    public ClubPrevResponseDTO(ClubPrevDTO clubPrevDTO, S3DownloadDto logo) {
        this.id = clubPrevDTO.getId();
        this.name = clubPrevDTO.getName();
        this.belongs = clubPrevDTO.getBelongs();
        this.briefActivityDescription = clubPrevDTO.getBriefActivityDescription();
        this.logo = logo;
    }
}
