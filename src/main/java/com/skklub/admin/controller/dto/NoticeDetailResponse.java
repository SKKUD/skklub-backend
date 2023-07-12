package com.skklub.admin.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.skklub.admin.service.dto.FileNames;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticeDetailResponse {
    private Long noticeId;
    private String title;
    private String content;
    private String writerName;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime createdAt;
    private Long prevNoticeId;
    private Long postNoticeId;
    private List<FileNames> extraFileNames = new ArrayList<>();
}
