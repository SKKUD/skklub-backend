package com.skklub.admin.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.skklub.admin.dto.S3DownloadDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Component
public class S3Uploader {

    private final AmazonS3Client amazonS3;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;


    public

    public String appendActivityImage(List<MultipartFile> multipartFiles) {

    }

    public String uploadLogoImage(MultipartFile multipartFile) {
        return upload(multipartFile);
    }

    private String upload(MultipartFile multipartFile) throws IOException {
        //이름 중복 확인
        String fileName = multipartFile.getOriginalFilename();


        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(multipartFile.getInputStream().available());

        amazonS3.putObject(bucket, fileName, multipartFile.getInputStream(), metadata);
        return amazonS3.getUrl(bucket, fileName).toString();
    }

    public S3DownloadDto download(String key) {
        S3Object s3Object = amazonS3.getObject(bucket, key);
        String contentType = s3Object.getObjectMetadata().getContentType();
        byte[] bytes = new byte[0];
        try {
            bytes = s3Object.getObjectContent().readAllBytes();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new S3DownloadDto(contentType, bytes);
    }
}
