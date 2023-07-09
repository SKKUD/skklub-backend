package com.skklub.admin.controller.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
public class NoticeIdAndDeletedNameResponse {
    public NoticeIdAndDeletedNameResponse(Long noticeId, String originalName) {
    }
}
