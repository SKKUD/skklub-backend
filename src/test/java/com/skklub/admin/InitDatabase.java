package com.skklub.admin;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.skklub.admin.controller.S3Transferer;
import com.skklub.admin.controller.dto.ClubCreateRequestDTO;
import com.skklub.admin.domain.*;
import com.skklub.admin.domain.enums.Role;
import com.skklub.admin.domain.imagefile.ActivityImage;
import com.skklub.admin.domain.imagefile.ExtraFile;
import com.skklub.admin.domain.imagefile.Logo;
import com.skklub.admin.domain.imagefile.Thumbnail;
import com.skklub.admin.service.dto.FileNames;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
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
import java.util.stream.Collectors;

import static java.lang.Thread.sleep;

@Slf4j
@Component
public class InitDatabase {

    @Autowired
    private InitTestData initTestData;

    @EventListener(ContextRefreshedEvent.class)
    public void init() throws IOException, InterruptedException {
        initTestData.init();
    }

    @EventListener(ContextClosedEvent.class)
    public void preDestroy() {
        initTestData.cleanS3();
    }

    @Component
    @Import(TestDataRepository.class)
    static class InitTestData {
        @PersistenceContext
        private EntityManager em;
        private final int clubCnt = 36;
        private final int noticeCnt = 20;
        private final int pendingClubCnt = 15;
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
        public void init() throws IOException, InterruptedException {
            readyDefaultLogoInS3();
            readyClubDomains();
            readyDefaultThumbnailInS3();
            readyNoticeDomains();
            readyMaster();
            readyPendingClubs();
        }

        private void readyMaster() {
            String encodedPw = bCryptPasswordEncoder.encode("testMasterPw");
            em.persist(
                    new User(
                            "testMasterID",
                            encodedPw,
                            Role.ROLE_MASTER,
                            "testMasterName",
                            "testMasterContact"
                    )
            );
        }


        private void readyPendingClubs() {
            for (int i = 0; i < pendingClubCnt; i++) {
                Role reqTo;
                switch (i % 3) {
                    case 0 -> reqTo = Role.ROLE_ADMIN_SEOUL_CENTRAL;
                    case 1 -> reqTo = Role.ROLE_ADMIN_SUWON_CENTRAL;
                    default -> reqTo = Role.ROLE_MASTER;
                }
                em.persist(
                        new PendingClub(
                                "testPendingName" + i,
                                "testBriefDescription" + i,
                                "testActivityDescription" + i,
                                "testClubDescription" + i,
                                "testUserId" + i,
                                "testPw" + i,
                                "testUser" + i,
                                "testContact" + i,
                                reqTo
                        )
                );
            }
        }

        private void readyNoticeDomains() throws IOException, InterruptedException {
            for (int i = 0; i < noticeCnt; i++) {
                User user = readyAdmin(i);
                em.persist(user);
                Thumbnail thumbnail = readyThumbnail(i);
                Notice notice = readyNotice(i, user, thumbnail);
                sleep(1000);
                em.persist(notice);
                List<ExtraFile> extraFiles = readyExtraFiles(notice, i);
                extraFiles.stream().forEach(em::persist);
            }
        }

        private List<ExtraFile> readyExtraFiles(Notice notice, int index) throws IOException {
            List<ExtraFile> extraFiles = new ArrayList<>();
            for (int i = 0; i < index % 5; i++) {
                String originalFileName = i + ".pdf";
                Path path = Paths.get("src/test/resources/file/" + originalFileName);
                byte[] bytes = Files.readAllBytes(path);
                MultipartFile multipartFile = new MockMultipartFile(originalFileName, originalFileName, "application/pdf", bytes);
                extraFiles.add(
                        s3Transferer.uploadOne(multipartFile).toExtraFileEntity()
                );
            }
            notice.appendExtraFiles(extraFiles);
            return extraFiles;
        }

        private Notice readyNotice(int i, User user, Thumbnail thumbnail) {
            return new Notice(
                    "test title " + i,
                    "test content " + i,
                    user,
                    thumbnail
            );
        }

        private Thumbnail readyThumbnail(int i) throws IOException {
            if(i % 4 == 0) return new Thumbnail(
                    "default_thumb.png",
                    "default_thumb.png"
            );
            Path path = Paths.get("src/test/resources/img/5.jpg");
            byte[] bytes = Files.readAllBytes(path);
            MultipartFile multipartFile = new MockMultipartFile("5.jpg", "5.jpg", "image", bytes);
            return s3Transferer.uploadOne(multipartFile).toThumbnailEntity();
        }

        private User readyAdmin(int i) {
            String password = bCryptPasswordEncoder.encode("testAdminPw" + i);
            return new User(
                    "testAdminID" + i,
                    password,
                    i % 2 == 0 ? Role.ROLE_ADMIN_SEOUL_CENTRAL : Role.ROLE_ADMIN_SUWON_CENTRAL,
                    "testAdminName" + i,
                    "010-0000-0000"
            );
        }

        private void readyClubDomains() throws IOException {
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

        private void readyDefaultThumbnailInS3() throws IOException {
            Path path = Paths.get("src/test/resources/img/default_thumb.png");
            byte[] bytes = Files.readAllBytes(path);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            MultipartFile multipartFile = new MockMultipartFile("default_thumb.png", "default_thumb.png","image", bytes);
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(multipartFile.getInputStream().available());
            amazonS3.putObject(bucket, "default_thumb.png", byteArrayInputStream, metadata);
        }

        private List<ActivityImage> readyActivityImages(int index) throws IOException {
            List<ActivityImage> activityImages = new ArrayList<>();
            for (int i = 0; i < index % 6; i++) {
                Path path = Paths.get("src/test/resources/img/4.jpg");
                byte[] bytes = Files.readAllBytes(path);
                MultipartFile multipartFile = new MockMultipartFile("4.jpg", "4.jpg", "image", bytes);
                FileNames fileNames = s3Transferer.uploadOne(multipartFile);
                fileNames.setOriginalName(i + ".jpg");
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
            ObjectListing objectListing = amazonS3.listObjects(bucket);
            List<String> keys = objectListing.getObjectSummaries().stream()
                    .map(S3ObjectSummary::getKey)
                    .collect(Collectors.toList());
            s3Transferer.deleteAll(keys);
        }
    }
}
