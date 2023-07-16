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
public class NoticePrevResponse {
    private Long noticeId;
    private String title;
    private String writerName;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime createdAt;

    public NoticePrevResponse(Notice notice) {
        this.noticeId = notice.getId();
        this.title = notice.getTitle();
        this.writerName = notice.getWriter().getName();
        this.createdAt = notice.getCreatedAt().truncatedTo(ChronoUnit.MINUTES);
    }
}
