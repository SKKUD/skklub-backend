package com.skklub.admin.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.skklub.admin.controller.dto.S3DownloadDto;
import com.skklub.admin.service.dto.FileNames;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.amazonaws.services.s3.model.DeleteObjectsRequest.KeyVersion;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3Transferer {

    private final AmazonS3 amazonS3;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    @Value("${cloud.aws.region.static}")
    private String area;

    public List<FileNames> uploadAll(List<MultipartFile> multipartFiles) {
        return multipartFiles.stream()
                .map(m -> upload(m))
                .collect(Collectors.toList());
    }

    public FileNames uploadOne(MultipartFile multipartFile) {
        return upload(multipartFile);
    }

    private FileNames upload(MultipartFile multipartFile) {
        //이름 중복 확인
        String ext = "." + multipartFile.getOriginalFilename().split("\\.(?=[^\\.]+$)")[1];
        String fileName = multipartFile.getOriginalFilename();
        String savedName = UUID.randomUUID() + ext;


        ObjectMetadata metadata = new ObjectMetadata();
        try {
            metadata.setContentLength(multipartFile.getInputStream().available());
            amazonS3.putObject(bucket, savedName, multipartFile.getInputStream(), metadata);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new FileNames(fileName, savedName);
    }

    public S3DownloadDto downloadOne(FileNames fileName) {
        if(!amazonS3.doesObjectExist(bucket, fileName.getSavedName())) throw new AmazonS3Exception("해당 이름의 객체가 버킷에 존재하지 않습니다.");
        String url = "https://s3." + area + ".amazonaws.com/" + bucket + "/" + fileName.getSavedName();
        return new S3DownloadDto(fileName.getId(), fileName.getOriginalName(), url);
    }

    public List<S3DownloadDto> downloadAll(List<FileNames> fileNames) {
        return fileNames.stream()
                .map(f -> downloadOne(f))
                .collect(Collectors.toList());
    }

    private S3DownloadDto download(String key) {
        return new S3DownloadDto();
    }

    public void deleteOne(String key) {
        amazonS3.deleteObject(bucket, key);
    }

    public void deleteAll(List<String> keys) {
        if(keys.isEmpty()) return;
        DeleteObjectsRequest multiObjectDeleteRequest = new DeleteObjectsRequest(bucket)
                .withKeys(keys.stream().map(KeyVersion::new).collect(Collectors.toList()))
                .withQuiet(false);
        amazonS3.deleteObjects(multiObjectDeleteRequest);
    }
}
