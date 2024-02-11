package com.skklub.admin.controller.dto.news;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClubMetaDTO {
    private String name;
    private String description;
    private String activityDescription;
    private Integer establishAt;
}
