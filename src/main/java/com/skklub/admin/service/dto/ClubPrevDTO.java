package com.skklub.admin.service.dto;

import com.skklub.admin.domain.Club;
import com.skklub.admin.domain.Logo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClubPrevDTO {
    private Long id;
    private String name;
    private String belongs;
    private String briefActivityDescription;
    private FileNames logo;

    public static ClubPrevDTO fromEntity(Club club) {
        return ClubPrevDTO.builder()
                .id(club.getId())
                .name(club.getName())
                .belongs(club.getBelongs())
                .briefActivityDescription(club.getBriefActivityDescription())
                .logo(new FileNames(club.getLogo()))
                .build();
    }
}
