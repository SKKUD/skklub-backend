package com.skklub.admin.repository.dto;

import com.querydsl.core.annotations.QueryProjection;
import com.skklub.admin.domain.Notice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
public class NoticeDetailDto {
    private Notice notice;
    private String writerName;
    private Long preNoticeId;
    private Long postNoticeId;

    @QueryProjection
    public NoticeDetailDto(Notice notice, String writerName, Long preNoticeId, Long postNoticeId) {
        this.notice = notice;
        this.writerName = writerName;
        this.preNoticeId = preNoticeId;
        this.postNoticeId = postNoticeId;
    }
}
