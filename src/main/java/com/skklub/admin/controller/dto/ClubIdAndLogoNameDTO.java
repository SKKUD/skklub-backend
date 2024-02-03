package com.skklub.admin.controller.dto;

import com.skklub.admin.domain.imagefile.Logo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClubIdAndLogoNameDTO {
    private Long clubId;
    private String logoOriginalName;
    private String logoSavedName;

    public ClubIdAndLogoNameDTO(Long clubId, Logo logo) {
        this.clubId = clubId;
        this.logoOriginalName = logo.getOriginalName();
        this.logoSavedName = logo.getUploadedName();
    }
}
