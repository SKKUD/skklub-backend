package com.skklub.admin.controller.dto.news;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClubFullInformationDTO {
    private Long club_operation_id;

    private ClubMetaDTO clubMetaDTO;
    private ClubOperationDTO clubOperationDTO;
    private ClubCategorizationDTO clubCategorizationDTO;
    private UserPublicInformationDTO userPublicInformationDTO;
}
