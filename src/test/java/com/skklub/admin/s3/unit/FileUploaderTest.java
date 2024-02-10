package com.skklub.admin.s3.unit;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.skklub.admin.domain.FileName;
import com.skklub.admin.s3.S3MockConfig;
import com.skklub.admin.service.FileUploader;
import io.findify.s3mock.S3Mock;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;
import scala.Int;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@Import(S3MockConfig.class)
public class FileUploaderTest {

    private static final String bucket = "mock.s3";

    @Autowired
    private AmazonS3 amazonS3;
    @Autowired
    private FileUploader fileUploader;
    private Random random = new Random();

    @BeforeAll
    static void setUp(@Autowired S3Mock s3Mock, @Autowired AmazonS3 amazonS3) {
        s3Mock.start();
        amazonS3.createBucket(bucket);
    }

    @AfterAll
    static void tearDown(@Autowired S3Mock s3Mock, @Autowired AmazonS3 amazonS3) {
        amazonS3.shutdown();
        s3Mock.stop();
    }

    @Test
    @DisplayName("Mock s3 import 테스트")
    void S3Import() throws IOException {
        // given
        String path = "test/02.txt";
        String contentType = "text/plain";
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(contentType);
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, path, new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)), objectMetadata);
        amazonS3.putObject(putObjectRequest);

        // when
        S3Object s3Object = amazonS3.getObject(bucket, path);

        // then
        Assertions.assertThat(s3Object.getObjectMetadata().getContentType()).isEqualTo(contentType);
        Assertions.assertThat(new String(FileCopyUtils.copyToByteArray(s3Object.getObjectContent()))).isEqualTo("");
    }

    /**
     * input : Random Byte File
     * expect result : Save Success & return With Random UUID
     */
    @Test
    @DisplayName("파일 한개 업로드 - 정상")
    public void uploadOne_Default_Success() throws Exception{
        //given
        String name = "testFIleName";
        String extension = "testFIleExtension";
        byte[] contents = new byte[1024];
        random.nextBytes(contents);
        MockMultipartFile file = new MockMultipartFile(name + "." + extension, contents);

        log.info("file : {}", file.getName());
        //mocking

        //when
        FileName fileName = fileUploader.uploadOne(file);

        //then
        assertThat("가상 파일명 생성 검증", fileName.getUploadedName(), containsString("." + extension));
        assertThat("원본 파일명 유지 검증", fileName.getOriginalName(), equalTo(name + "." + extension));
        assertThat("파일 저장 검증", amazonS3.doesObjectExist(bucket, fileName.getUploadedName()), equalTo(true));
    }

    /**
     * input : 100 Random Byte Files
     * expect result : Save Success & return With Random UUID
     */
    @Test
    @DisplayName("파일 여러개 업로드 - 정상")
    public void uploadAll_Default_Success() throws Exception{
        //given\
        Integer fileCount = 100;
        List<MultipartFile> files = new ArrayList<>();
        String extension = "testFIleExtension";
        byte[] contents = new byte[1024];
        String testFIleNamePrefix = "testFIleName";
        for (int i = 0; i < fileCount; i++) {
            String name = testFIleNamePrefix + i;
            random.nextBytes(contents);
            files.add(new MockMultipartFile(name + "." + extension, contents));
        }

        //mocking

        //when
        List<FileName> fileNames = fileUploader.uploadAll(files);

        //then
        assertThat("변환된 파일 총 개수 검증", fileNames.size(), equalTo(fileCount));
        List<String> multipartFileNames = files.stream()
                .map(MultipartFile::getName)
                .collect(Collectors.toList());
        List<String> savedOriginalNames = fileNames.stream()
                .map(FileName::getOriginalName)
                .collect(Collectors.toList());
        assertThat("원본 파일명 유지 검증 - List contains", multipartFileNames.containsAll(savedOriginalNames), equalTo(true));
        savedOriginalNames
                .forEach(originalFileName -> {
                    assertThat("원본 파일명 유지 검증 - Prefix", originalFileName, containsString(testFIleNamePrefix));
                });
        fileNames.stream()
                .map(FileName::getUploadedName)
                .forEach(uploadFileName -> {
                    assertThat("가상 파일명 생성 검증", uploadFileName, containsString("." + extension));
                    assertThat("파일 저장 검증", amazonS3.doesObjectExist(bucket, uploadFileName), equalTo(true));
                });

    }
}