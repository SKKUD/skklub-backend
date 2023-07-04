package com.skklub.admin;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.skklub.admin.controller.S3Transferer;
import com.skklub.admin.controller.dto.ClubCreateRequestDTO;
import com.skklub.admin.domain.ActivityImage;
import com.skklub.admin.domain.Club;
import com.skklub.admin.domain.Logo;
import com.skklub.admin.domain.Recruit;
import com.skklub.admin.domain.enums.Campus;
import com.skklub.admin.domain.enums.ClubType;
import com.skklub.admin.service.dto.FileNames;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class TestConfig {

    @Autowired
    private InitTestData initTestData;

    @PostConstruct
    public void init() throws IOException {
        initTestData.init();
    }

    @PreDestroy
    public void preDestroy() {
        initTestData.cleanS3();
    }

    @Component
    static class InitTestData {
        @PersistenceContext
        private EntityManager em;
        private final int clubCnt = 36;
        @Autowired
        private S3Transferer s3Transferer;
        @Autowired
        private AmazonS3 amazonS3;
        @Value("${cloud.aws.s3.bucket}")
        private String bucket;

        @Transactional
        public void init() throws IOException {
            readyDefaultLogoInS3();
            for (int i = 0; i < clubCnt; i++) {
                Logo logo = readyLogo(i);
                Optional<Recruit> recruit = readyRecruit(i);
                Club club = readyClub(i);
                List<ActivityImage> activityImages = readyActivityImages(i);
                club.changeLogo(logo);
                club.appendActivityImages(activityImages);
                recruit.ifPresent(r -> {
                            club.startRecruit(r);
                            em.persist(r);
                        }
                );
                em.persist(club);
                activityImages.stream().forEach(em::persist);
            }
        }

        private void readyDefaultLogoInS3() throws IOException {
            Path path = Paths.get("src/test/resources/img/alt.jpg");
            byte[] bytes = Files.readAllBytes(path);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            MultipartFile multipartFile = new MockMultipartFile("alt.jpg", "alt.jpg","image", bytes);
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(multipartFile.getInputStream().available());
            amazonS3.putObject(bucket, "alt.jpg", byteArrayInputStream, metadata);
        }

        private List<ActivityImage> readyActivityImages(int index) throws IOException {
            List<ActivityImage> activityImages = new ArrayList<>();
            for (int i = 0; i < index % 6; i++) {
                Path path = Paths.get("src/test/resources/img/4.jpg");
                byte[] bytes = Files.readAllBytes(path);
                MultipartFile multipartFile = new MockMultipartFile("4.jpg", "4.jpg", "image", bytes);
                FileNames fileNames = s3Transferer.uploadOne(multipartFile);
                activityImages.add(fileNames.toActivityImageEntity());
            }
            return activityImages;
        }

        private Logo readyLogo(int index) throws IOException {
            if(index % 2 != 0) {
                Path path = Paths.get("src/test/resources/img/3.jpg");
                byte[] bytes = Files.readAllBytes(path);
                MultipartFile multipartFile = new MockMultipartFile("3.jpg", "3.jpg", "image", bytes);
                return s3Transferer.uploadOne(multipartFile).toLogoEntity();
            }
            return new FileNames("alt.jpg", "alt.jpg").toLogoEntity();
        }

        private Club readyClub(int index) {
            String clubName = "testClubName" + index;
            String activityDescription = "testActivityDescription" + index;
            String briefActivityDescription = "testBriefActivityDescription" + index;
            String clubDescription = "testClubDescription" + index;
            String belongs = "평면예술";
            Campus campus = Campus.명륜;
            ClubType clubType = ClubType.중앙동아리;
            Integer establishDate = 1398 + index;
            String headLine = "testHeadLine" + index;
            String mandatoryActivatePeriod = "testMandatoryActivatePeriod" + index;
            Integer memberAmount = 60 + index;
            String regularMeetingTime = "testRegularMeetingTime" + index;
            String roomLocation = "testRoomLocation" + index;
            String webLink1 = "testWebLink1_" + index;
            String webLink2 = "testWebLink2_" + index;

            ClubCreateRequestDTO clubCreateRequestDTO = ClubCreateRequestDTO.builder()
                    .clubName(clubName)
                    .activityDescription(activityDescription)
                    .briefActivityDescription(briefActivityDescription)
                    .clubDescription(clubDescription)
                    .belongs(belongs)
                    .campus(campus)
                    .clubType(clubType)
                    .establishDate(establishDate)
                    .headLine(headLine)
                    .mandatoryActivatePeriod(mandatoryActivatePeriod)
                    .memberAmount(memberAmount)
                    .regularMeetingTime(regularMeetingTime)
                    .roomLocation(roomLocation)
                    .webLink1(webLink1)
                    .webLink2(webLink2)
                    .build();
            return clubCreateRequestDTO.toEntity();
        }

        private Optional<Recruit> readyRecruit(int index) {
            return Optional.ofNullable(
                    switch (index % 3) {
                        case 2 -> new Recruit(LocalDateTime.now(), LocalDateTime.now(),
                                index + "명", "Test Recruit Process_" + index,
                                "010-" + String.valueOf(index % 3).repeat(4) + "-" + String.valueOf(index % 3).repeat(4),
                                "Test Recruit web_" + index);
                        case 1 -> new Recruit(null, null,
                                index + "명", "Test Recruit Process_" + index,
                                "010-" + String.valueOf(index % 3).repeat(4) + "-" + String.valueOf(index % 3).repeat(4),
                                "Test Recruit web_" + index);
                        default -> null;
                    }
            );

        }

        @Transactional
        public void cleanS3() {
            s3Transferer.deleteOne("alt.jpg");
            cleanLogoInS3();
            cleanActivityImagesInS3();
        }

        private void cleanActivityImagesInS3() {
            List<String> uploadedActivityImages = em.createQuery("select a.uploadedName from ActivityImage a", String.class)
                    .getResultList();
            uploadedActivityImages.stream()
                    .forEach(s3Transferer::deleteOne);
        }

        private void cleanLogoInS3() {
            List<String> uploadedLogo = em.createQuery("select l.uploadedName from Logo l", String.class)
                    .getResultList();
            uploadedLogo.stream()
                    .forEach(s3Transferer::deleteOne);
        }
    }
}
