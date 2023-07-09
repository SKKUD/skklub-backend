package com.skklub.admin.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticeDeletionDto {
    private String noticeTitle;
    private FileNames thumbnailFileName;
    private List<FileNames> extraFileNames = new ArrayList<>();
}
