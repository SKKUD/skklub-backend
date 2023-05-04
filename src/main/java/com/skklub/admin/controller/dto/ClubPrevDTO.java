package com.skklub.admin.controller.dto;

import com.skklub.admin.domain.enums.ActivityType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClubPrevDTO {
    private Long id;
    private String name;
    private MultipartFile logo;
    private ActivityType activityType;
    private String briefActivityDescription;

}
