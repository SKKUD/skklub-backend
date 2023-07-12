package com.skklub.admin.controller.dto;

import com.skklub.admin.domain.Notice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticeIdAndTitleResponse {
    private Long id;
    private String title;

    public NoticeIdAndTitleResponse(Notice notice) {
        this.id = notice.getId();
        this.title = notice.getTitle();
    }
}
