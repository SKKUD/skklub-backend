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
    private String bytes;

    public S3DownloadDto(byte[] bytes) {
        byte[] encode = Base64.getEncoder().encode(bytes);
        try {
            this.bytes = new String(encode, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
