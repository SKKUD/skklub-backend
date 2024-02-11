package com.skklub.admin.controller.dto.news;

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
public class ClubCategorizationDTO {
    private Campus campus;
    private ClubType clubType;
    private String belongs;
    private String activityType;
}
