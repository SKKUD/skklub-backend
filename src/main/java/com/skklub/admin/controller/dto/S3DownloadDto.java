package com.skklub.admin.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class S3DownloadDto {
    private Long id;
    private String fileName;
    private byte[] bytes;

    public S3DownloadDto(byte[] bytes) {
        this.bytes = bytes;
    }
}
