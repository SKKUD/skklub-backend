package com.skklub.admin.service;

import com.amazonaws.services.s3.AmazonS3;
import com.skklub.admin.domain.FileName;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploader {
    private final AmazonS3 amazonS3;

    public FileName uploadOne(MultipartFile multipartFile) {

    }

    public List<FileName> uploadAll(MultipartFile... multipartFiles) {

    }

}