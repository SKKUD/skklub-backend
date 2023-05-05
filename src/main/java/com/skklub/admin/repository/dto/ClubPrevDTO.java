package com.skklub.admin.repository.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClubPrevDTO {
    private Long id;
    private String name;
    private String belongs;
    private String briefActivityDescription;
    private String logoOriginalName;
    private String logoUploadedName;
}
