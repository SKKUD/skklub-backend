package com.skklub.admin;

import ch.qos.logback.core.pattern.color.BoldCyanCompositeConverter;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.skklub.admin.controller.S3Transferer;
import com.skklub.admin.controller.dto.ClubCreateRequestDTO;
import com.skklub.admin.domain.*;
import com.skklub.admin.domain.enums.Role;
import com.skklub.admin.service.dto.FileNames;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
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
public class InitDatabase {

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
    @Import(TestDataRepository.class)
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
        @Autowired
        private TestDataRepository testDataRepository;
        @Autowired
        private BCryptPasswordEncoder bCryptPasswordEncoder;

        @Transactional
        public void init() throws IOException {
            readyDefaultLogoInS3();
            for (int i = 0; i < clubCnt; i++) {
                Logo logo = readyLogo(i);
                Optional<Recruit> recruit = readyRecruit(i);
                User user = readyUser(i);
                Club club = readyClub(i);
                List<ActivityImage> activityImages = readyActivityImages(i);
                club.changeLogo(logo);
                club.setUser(user);
                em.persist(user);
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
            ClubCreateRequestDTO clubCreateRequestDTO = testDataRepository.getClubCreateRequestDTO(index);

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

        private User readyUser(int index) {
            String password = bCryptPasswordEncoder.encode("password" + index);
            return new User("userId" + index,
                    password,
                    Role.ROLE_USER,
                    "user_" + index,
                    "user contact_" + index);

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
