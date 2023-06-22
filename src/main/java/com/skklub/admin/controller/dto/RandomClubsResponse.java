package com.skklub.admin.controller.dto;

import com.skklub.admin.domain.Club;
import com.skklub.admin.domain.enums.Campus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RandomClubsResponse {
    private Long id;
    private String name;
    private Campus campus;

    public RandomClubsResponse(Club club) {
        this.id = club.getId();
        this.name = club.getName();
        this.campus = club.getCampus();
    }
}
