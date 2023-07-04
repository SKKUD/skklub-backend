package com.skklub.admin.service.dto;

import com.skklub.admin.domain.ActivityImage;
import com.skklub.admin.domain.ExtraFile;
import com.skklub.admin.domain.Logo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileNames {
    private Long id;
    private String originalName;
    private String savedName;

    public Logo toLogoEntity() {
        return new Logo(originalName, savedName);
    }
    public ActivityImage toActivityImageEntity() {
        return new ActivityImage(originalName, savedName);
    }
    public ExtraFile toExtraFileEntity(){
        return new ExtraFile(originalName, savedName);
    }

    public FileNames(String originalName, String savedName) {
        this.originalName = originalName;
        this.savedName = savedName;
    }

    public FileNames(Logo logo) {
        this.id = logo.getId();
        this.originalName = logo.getOriginalName();
        this.savedName = logo.getUploadedName();
    }
    public FileNames(ActivityImage activityImage) {
        this.id = activityImage.getId();
        this.originalName = activityImage.getOriginalName();
        this.savedName = activityImage.getUploadedName();
    }
}
