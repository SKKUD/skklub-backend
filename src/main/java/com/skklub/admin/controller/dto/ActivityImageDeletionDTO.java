package com.skklub.admin.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityImageDeletionDTO {
    private Long clubId;
    private String clubName;
    private String deletedActivityImageName;
}
