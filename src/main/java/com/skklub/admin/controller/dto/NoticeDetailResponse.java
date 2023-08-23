package com.skklub.admin.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.skklub.admin.domain.Notice;
import com.skklub.admin.service.dto.FileNames;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private Optional<NoticeIdAndTitleResponse> preNotice;
    private Optional<NoticeIdAndTitleResponse> postNotice;
    @Builder.Default
    private List<FileNames> extraFileNames = new ArrayList<>();

    public NoticeDetailResponse(Notice notice, Optional<Notice> preNotice, Optional<Notice> postNotice) {
        this.noticeId = notice.getId();
        this.title = notice.getTitle();
        this.content = notice.getContent();
        this.writerName = notice.getWriter().getName();
        this.extraFileNames = notice.getExtraFiles().stream()
                .map(FileNames::new)
                .collect(Collectors.toList());
        this.createdAt = notice.getCreatedAt();
        this.preNotice = preNotice.map(NoticeIdAndTitleResponse::new);
        this.postNotice = postNotice.map(NoticeIdAndTitleResponse::new);
    }
}
