package com.skklub.admin.controller.dto;

import com.skklub.admin.domain.Notice;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class NoticeCreateRequest {
    @NotBlank
    private String title;
    @NotBlank
    private String content;

    public Notice toEntity() {
        return new Notice(title, content, null, null);
    }
}
