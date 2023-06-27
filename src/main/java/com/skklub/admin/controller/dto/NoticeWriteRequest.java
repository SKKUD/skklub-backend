package com.skklub.admin.controller.dto;

import com.skklub.admin.domain.Notice;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticeWriteRequest {
    @NotBlank
    private String title;
    @NotBlank
    private String content;
}
