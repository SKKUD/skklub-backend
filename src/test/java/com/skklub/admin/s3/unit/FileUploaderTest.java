package com.skklub.admin.s3.unit;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.skklub.admin.controller.S3Transferer;
import com.skklub.admin.s3.S3MockConfig;
import com.skklub.admin.service.dto.FileNames;
import io.findify.s3mock.S3Mock;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.persistence.PostRemove;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.FileCopyUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

@Import(S3MockConfig.class)
@ActiveProfiles("test")
@SpringBootTest
public class FileUploaderTest {

    @Autowired
    private AmazonS3 amazonS3;

    @Autowired
    private static final String BUCKET_NAME = "wedul";

    @BeforeAll
    static void setUp(@Autowired S3Mock s3Mock, @Autowired AmazonS3 amazonS3) {
        s3Mock.start();
        amazonS3.createBucket(BUCKET_NAME);
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
        PutObjectRequest putObjectRequest = new PutObjectRequest(BUCKET_NAME, path, new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)), objectMetadata);
        amazonS3.putObject(putObjectRequest);

        // when
        S3Object s3Object = amazonS3.getObject(BUCKET_NAME, path);

        // then
        assertThat(s3Object.getObjectMetadata().getContentType()).isEqualTo(contentType);
        assertThat(new String(FileCopyUtils.copyToByteArray(s3Object.getObjectContent()))).isEqualTo("");
    }
    
    /**
     * input :
     * expect result :
     */
    @Test
    @DisplayName("")
    public void () throws Exception{
        //given
        
        //mocking
        
        //when
        
        //then
        
    }

}