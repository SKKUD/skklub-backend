package com.skklub.admin.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticeIdAndFileNamesResponse {
    private Long noticeId;
    private String deletedFileName;
    private String changedFileName;
}
