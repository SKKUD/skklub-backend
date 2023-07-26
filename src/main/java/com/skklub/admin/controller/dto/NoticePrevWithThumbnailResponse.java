package com.skklub.admin.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.skklub.admin.domain.Notice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticePrevWithThumbnailResponse {
    private Long noticeId;
    private String title;
    private String content;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime createdAt;
    private S3DownloadDto thumbnail;

    public NoticePrevWithThumbnailResponse(Notice notice, S3DownloadDto thumbnail) {
        this.noticeId = notice.getId();
        this.title = notice.getTitle();
        this.content = notice.getContent();
        this.createdAt = notice.getCreatedAt();
        this.thumbnail = thumbnail;
    }
}
