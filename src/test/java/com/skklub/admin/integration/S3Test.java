package com.skklub.admin.integration;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.skklub.admin.controller.S3Transferer;
import com.skklub.admin.controller.dto.S3DownloadDto;
import com.skklub.admin.service.dto.FileNames;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@SpringBootTest
public class S3Test {
    @Autowired
    private S3Transferer s3Transferer;
    @Autowired
    private AmazonS3 amazonS3;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Test
    public void downloadDefault_SaveImgToResources() throws Exception{
        //given
        S3DownloadDto s3DownloadDto = s3Transferer.downloadOne(new FileNames(0L, "alt.jpg", "alt.jpg"));
        byte[] bytes = s3DownloadDto.getBytes();
//        S3Object object = amazonS3.getObject(bucket, "alt.jpg");
//        byte[] bytes = object.getObjectContent().readAllBytes();

        //when
        InputStream inputStream = new ByteArrayInputStream(bytes);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(inputStream.available());
        amazonS3.putObject(bucket, "testDefaultLogo.jpg", inputStream, metadata);

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        BufferedImage bufferedImage = ImageIO.read(byteArrayInputStream);
        ImageIO.write(bufferedImage, "jpg", new File("src/test/resources/img/defaultLogo.jpg"));

        //then
        //"src/test/resources/img/defaultLogo.jpg" 경로 확인
    }

    @Test
    public void download_WhenNoKey_AmazonS3Exception() throws Exception{
        //given

        //when
        Assertions.assertThrows(
                AmazonS3Exception.class,
                () -> s3Transferer.downloadOne(new FileNames("test.jpg", "NeverSavedImage.jpg")));

        //then

    }

    @Test
    public void uploadSome_FromResources_SaveImgToResources() throws Exception{
        //given
        Path path = Paths.get("src/test/resources/img/1.jpg");
        byte[] bytes = Files.readAllBytes(path);
        MultipartFile multipartFile = new MockMultipartFile("1.jpg", "1.jpg","image", bytes);
        FileNames fileNames = s3Transferer.uploadOne(multipartFile);

        //when
        log.info("multipartFile : {}", multipartFile.getOriginalFilename());
        log.info("fileNames.getOriginalName() : {}", fileNames.getOriginalName());
        log.info("fileNames.getSavedName() : {}", fileNames.getSavedName());

        S3DownloadDto s3DownloadDto = s3Transferer.downloadOne(new FileNames("1.jpg", fileNames.getSavedName()));
        byte[] s3DownloadDtoBytes = s3DownloadDto.getBytes();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(s3DownloadDtoBytes);
        BufferedImage bufferedImage = ImageIO.read(byteArrayInputStream);
        ImageIO.write(bufferedImage, "jpg", new File("src/test/resources/img/as" + s3DownloadDto.getFileName()));

        //then
        //See S3...
    }
}
