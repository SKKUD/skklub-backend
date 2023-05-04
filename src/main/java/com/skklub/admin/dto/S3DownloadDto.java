package com.skklub.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class S3DownloadDto {
    private String contentTYpe;
    private byte[] bytes;
}
