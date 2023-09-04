package com.skklub.admin.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.skklub.admin.domain.Notice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    private List<S3DownloadDto> extraFileDownloadDtos = new ArrayList<>();

    public NoticeDetailResponse(Notice notice, List<S3DownloadDto> s3DownloadDtos, Optional<Notice> preNotice, Optional<Notice> postNotice) {
        this.noticeId = notice.getId();
        this.title = notice.getTitle();
        this.content = notice.getContent();
        this.writerName = notice.getWriter().getName();
        this.extraFileDownloadDtos = s3DownloadDtos;
        this.createdAt = notice.getCreatedAt();
        this.preNotice = preNotice.map(NoticeIdAndTitleResponse::new);
        this.postNotice = postNotice.map(NoticeIdAndTitleResponse::new);
    }
}
