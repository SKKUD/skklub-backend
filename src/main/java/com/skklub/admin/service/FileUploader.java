package com.skklub.admin.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.skklub.admin.domain.FileName;
import com.skklub.admin.exception.ServerSideException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploader {
    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    @Value("${cloud.aws.region.static}")
    private String area;

    public FileName uploadOne(MultipartFile multipartFile) throws ServerSideException {
        String extension = "." + multipartFile.getName().split("\\.(?=[^\\.]+$)")[1];
        String uuid =  UUID.randomUUID().toString();
        String uploadName = uuid + extension;

        ObjectMetadata metadata = new ObjectMetadata();
        try {
            metadata.setContentLength(multipartFile.getInputStream().available());
            amazonS3.putObject(bucket, uploadName, multipartFile.getInputStream(), metadata);
        } catch (IOException e) {
            log.error("S3 파일 업로드 중 MultipartFile 인식 오류 발생");
            throw new ServerSideException();
        }
        return new FileName(multipartFile.getName(), uploadName);
    }


    public List<FileName> uploadAll(List<MultipartFile> multipartFiles) {
        return null;
    }

}
