package com.skklub.admin.integration.club;

import com.skklub.admin.TestDataRepository;
import com.skklub.admin.WithMockCustomUser;
import com.skklub.admin.controller.ClubController;
import com.skklub.admin.controller.S3Transferer;
import com.skklub.admin.controller.dto.ClubCreateRequestDTO;
import com.skklub.admin.controller.dto.ClubNameAndIdDTO;
import com.skklub.admin.controller.dto.S3DownloadDto;
import com.skklub.admin.domain.ActivityImage;
import com.skklub.admin.domain.Club;
import com.skklub.admin.domain.enums.Role;
import com.skklub.admin.exception.deprecated.error.exception.ClubIdMisMatchException;
import com.skklub.admin.exception.deprecated.error.exception.InvalidBelongsException;
import com.skklub.admin.repository.ActivityImageRepository;
import com.skklub.admin.repository.ClubRepository;
import com.skklub.admin.repository.LogoRepository;
import com.skklub.admin.service.dto.FileNames;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
@SpringBootTest
@Transactional
@Import(TestDataRepository.class)
@WithMockCustomUser(username = "testMasterID",role = Role.ROLE_MASTER)
public class ClubCreateIntegrationTest {
    @Autowired
    private ClubController clubController;
    @Autowired
    private S3Transferer s3Transferer;
    @Autowired
    private ClubRepository clubRepository;
    @Autowired
    private LogoRepository logoRepository;
    @Autowired
    private TestDataRepository testDataRepository;
    @Autowired
    private ActivityImageRepository activityImageRepository;
    @Autowired
    private EntityManager em;

//    @Test
    public void createClub_FullDataWithLogo_S3UploadDownloadDeleteAndLogoSettingAnd() throws Exception{
        //given
        ClubCreateRequestDTO clubCreateRequestDTO = testDataRepository.getClubCreateRequestDTO();

        Path path = Paths.get("src/test/resources/img/1.jpg");
        byte[] bytes = Files.readAllBytes(path);
        String logoName = "1.jpg";
        MockMultipartFile logo = new MockMultipartFile("logo", logoName, "image/jpeg", bytes);

        //when
        ClubNameAndIdDTO clubNameAndIdDTO = clubController.createClub(clubCreateRequestDTO, logo);
        Optional<Club> club = clubRepository.findById(clubNameAndIdDTO.getId());

        //then
        Assertions.assertThat(clubNameAndIdDTO.getName()).isEqualTo(clubCreateRequestDTO.getClubName());
        club.ifPresent(c -> {
            Assertions.assertThat(c.getLogo().getId()).isNotNull();
            Assertions.assertThat(c.getLogo().getOriginalName()).isEqualTo(logoName);
            S3DownloadDto s3DownloadDto = s3Transferer.downloadOne(new FileNames(c.getLogo().getId(), logoName, c.getLogo().getUploadedName()));
            saveImgToResources(s3DownloadDto, "createClubTest");
            s3Transferer.deleteOne(c.getLogo().getUploadedName());

            Assertions.assertThat(c.getId()).isNotNull();
            Assertions.assertThat(c.getName()).isEqualTo(clubCreateRequestDTO.getClubName());
            Assertions.assertThat(c.getActivityDescription()).isEqualTo(clubCreateRequestDTO.getActivityDescription());
            Assertions.assertThat(c.getBriefActivityDescription()).isEqualTo(clubCreateRequestDTO.getBriefActivityDescription());
            Assertions.assertThat(c.getClubDescription()).isEqualTo(clubCreateRequestDTO.getClubDescription());
            Assertions.assertThat(c.getBelongs()).isEqualTo(clubCreateRequestDTO.getBelongs());
            Assertions.assertThat(c.getCampus()).isEqualTo(clubCreateRequestDTO.getCampus());
            Assertions.assertThat(c.getClubType()).isEqualTo(clubCreateRequestDTO.getClubType());
            Assertions.assertThat(c.getEstablishAt()).isEqualTo(clubCreateRequestDTO.getEstablishDate());
            Assertions.assertThat(c.getHeadLine()).isEqualTo(clubCreateRequestDTO.getHeadLine());
            Assertions.assertThat(c.getMandatoryActivatePeriod()).isEqualTo(clubCreateRequestDTO.getMandatoryActivatePeriod());
            Assertions.assertThat(c.getMemberAmount()).isEqualTo(clubCreateRequestDTO.getMemberAmount());
            Assertions.assertThat(c.getRegularMeetingTime()).isEqualTo(clubCreateRequestDTO.getRegularMeetingTime());
            Assertions.assertThat(c.getRoomLocation()).isEqualTo(clubCreateRequestDTO.getRoomLocation());
            Assertions.assertThat(c.getWebLink1()).isEqualTo(clubCreateRequestDTO.getWebLink1());
            Assertions.assertThat(c.getWebLink2()).isEqualTo(clubCreateRequestDTO.getWebLink2());
        });
    }

//    @Test
    public void createClub_FullDataWithNoLogo_S3UploadDownloadDeleteAndLogoSettingAnd() throws Exception{
        //given
        ClubCreateRequestDTO clubCreateRequestDTO = testDataRepository.getClubCreateRequestDTO();
        String logoName = "alt.jpg";

        //when
        ClubNameAndIdDTO clubNameAndIdDTO = clubController.createClub(clubCreateRequestDTO, null);
        Optional<Club> club = clubRepository.findById(clubNameAndIdDTO.getId());

        //then
        Assertions.assertThat(clubNameAndIdDTO.getName()).isEqualTo(clubCreateRequestDTO.getClubName());
        club.ifPresent(c -> {
            Assertions.assertThat(c.getLogo().getId()).isNotNull();
            Assertions.assertThat(c.getLogo().getOriginalName()).isEqualTo(logoName);
            Assertions.assertThat(c.getLogo().getUploadedName()).isEqualTo(logoName);
            S3DownloadDto s3DownloadDto = s3Transferer.downloadOne(new FileNames(c.getLogo().getId(), logoName, c.getLogo().getUploadedName()));
            saveImgToResources(s3DownloadDto, "createClubTest");
            Assertions.assertThat(c.getId()).isNotNull();
            Assertions.assertThat(c.getName()).isEqualTo(clubCreateRequestDTO.getClubName());
            Assertions.assertThat(c.getActivityDescription()).isEqualTo(clubCreateRequestDTO.getActivityDescription());
            Assertions.assertThat(c.getBriefActivityDescription()).isEqualTo(clubCreateRequestDTO.getBriefActivityDescription());
            Assertions.assertThat(c.getClubDescription()).isEqualTo(clubCreateRequestDTO.getClubDescription());
            Assertions.assertThat(c.getBelongs()).isEqualTo(clubCreateRequestDTO.getBelongs());
            Assertions.assertThat(c.getCampus()).isEqualTo(clubCreateRequestDTO.getCampus());
            Assertions.assertThat(c.getClubType()).isEqualTo(clubCreateRequestDTO.getClubType());
            Assertions.assertThat(c.getEstablishAt()).isEqualTo(clubCreateRequestDTO.getEstablishDate());
            Assertions.assertThat(c.getHeadLine()).isEqualTo(clubCreateRequestDTO.getHeadLine());
            Assertions.assertThat(c.getMandatoryActivatePeriod()).isEqualTo(clubCreateRequestDTO.getMandatoryActivatePeriod());
            Assertions.assertThat(c.getMemberAmount()).isEqualTo(clubCreateRequestDTO.getMemberAmount());
            Assertions.assertThat(c.getRegularMeetingTime()).isEqualTo(clubCreateRequestDTO.getRegularMeetingTime());
            Assertions.assertThat(c.getRoomLocation()).isEqualTo(clubCreateRequestDTO.getRoomLocation());
            Assertions.assertThat(c.getWebLink1()).isEqualTo(clubCreateRequestDTO.getWebLink1());
            Assertions.assertThat(c.getWebLink2()).isEqualTo(clubCreateRequestDTO.getWebLink2());
        });
    }

    private void saveImgToResources(S3DownloadDto s3DownloadDto, String fileName) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(s3DownloadDto.getUrl().getBytes());
        try {
            BufferedImage bufferedImage = ImageIO.read(byteArrayInputStream);
            ImageIO.write(bufferedImage, "jpg", new File("src/test/resources/img/" + fileName + ".jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    @Test
    public void createClub_WrongBelongsWithLogo_SkipSavingAndInvalidBelongsException() throws Exception{
        //given
        ClubCreateRequestDTO clubCreateRequestDTO = testDataRepository.getClubCreateRequestDTO();
        MockMultipartFile logo = getLogoMockMultipartFile();
        Field declaredField = clubCreateRequestDTO.getClass().getDeclaredField("belongs");
        declaredField.setAccessible(true);
        declaredField.set(clubCreateRequestDTO, "wrongBelongs");

        //when
        assertThrows(InvalidBelongsException.class, () -> clubController.createClub(clubCreateRequestDTO, logo));
    }

    private MockMultipartFile getLogoMockMultipartFile() throws IOException {
        Path path = Paths.get("src/test/resources/img/1.jpg");
        byte[] bytes = Files.readAllBytes(path);
        String logoName = "1.jpg";
        MockMultipartFile logo = new MockMultipartFile("logo", logoName, "image/jpeg", bytes);
        return logo;
    }

    @Test
    public void appendActImg_AppendWhenNoImgs_SavedToS3WellAndCanFindClubId() throws Exception {
        //given
        ClubCreateRequestDTO clubCreateRequestDTO = testDataRepository.getClubCreateRequestDTO();
        MultipartFile logo = getLogoMockMultipartFile();
        ClubNameAndIdDTO clubNameAndId = clubController.createClub(clubCreateRequestDTO, logo);
        List<MultipartFile> activities = new ArrayList<>();
        int actImgCnt = 10;
        for(int i = 0; i < actImgCnt; i++){
            activities.add(getLogoMockMultipartFile());
        }

        //when
        ResponseEntity<ClubNameAndIdDTO> response = clubController.uploadActivityImages(clubNameAndId.getId(), activities);

        //then
        Assertions.assertThat(clubNameAndId.getName()).isEqualTo(response.getBody().getName());
        Assertions.assertThat(clubNameAndId.getId()).isEqualTo(response.getBody().getId());
        List<ActivityImage> savedActivityImgList = em.createQuery("select a from ActivityImage a where a.club.id = :club")
                .setParameter("club", clubNameAndId.getId())
                .getResultList();

        Assertions.assertThat(savedActivityImgList.size()).isEqualTo(actImgCnt);
        savedActivityImgList.stream()
                .forEach(a -> {
                    Assertions.assertThat(a.getClub().getId()).isEqualTo(clubNameAndId.getId());
                    Assertions.assertThat(a.getOriginalName()).isEqualTo(logo.getOriginalFilename());
                });
        assertDoesNotThrow(() -> s3Transferer.downloadAll(savedActivityImgList.stream().map(FileNames::new).collect(Collectors.toList())));
    }

    @Test
    public void appendActImg_AppendWhenExistingImgs_SavedToS3WellAndCanFindClubId() throws Exception {
        //given
        ClubCreateRequestDTO clubCreateRequestDTO = testDataRepository.getClubCreateRequestDTO();
        MultipartFile logo = getLogoMockMultipartFile();
        ClubNameAndIdDTO clubNameAndId = clubController.createClub(clubCreateRequestDTO, logo);
        List<MultipartFile> activities = new ArrayList<>();
        int actImgCnt = 10;
        for(int i = 0; i < actImgCnt; i++){
            activities.add(getLogoMockMultipartFile());
        }
        ResponseEntity<ClubNameAndIdDTO> temp = clubController.uploadActivityImages(clubNameAndId.getId(), activities);

        //when
        ResponseEntity<ClubNameAndIdDTO> response = clubController.uploadActivityImages(clubNameAndId.getId(), activities);

        //then
        Assertions.assertThat(clubNameAndId.getName()).isEqualTo(response.getBody().getName());
        Assertions.assertThat(clubNameAndId.getId()).isEqualTo(response.getBody().getId());
        List<ActivityImage> savedActivityImgList = em.createQuery("select a from ActivityImage a where a.club.id = :club")
                .setParameter("club", clubNameAndId.getId())
                .getResultList();

        Assertions.assertThat(savedActivityImgList.size()).isEqualTo(actImgCnt * 2);
        savedActivityImgList.stream()
                .forEach(a -> {
                    Assertions.assertThat(a.getClub().getId()).isEqualTo(clubNameAndId.getId());
                    Assertions.assertThat(a.getOriginalName()).isEqualTo(logo.getOriginalFilename());
                });
        assertDoesNotThrow(() -> s3Transferer.downloadAll(savedActivityImgList.stream().map(FileNames::new).collect(Collectors.toList())));
    }

    @Test
    public void appendActImg_WhenListIsEmpty_SavedToS3WellAndCanFindClubId() throws Exception {
        //given
        ClubCreateRequestDTO clubCreateRequestDTO = testDataRepository.getClubCreateRequestDTO();
        MultipartFile logo = getLogoMockMultipartFile();
        ClubNameAndIdDTO clubNameAndId = clubController.createClub(clubCreateRequestDTO, logo);
        List<MultipartFile> activities = new ArrayList<>();
        int actImgCnt = 0;

        //when
        ResponseEntity<ClubNameAndIdDTO> response = clubController.uploadActivityImages(clubNameAndId.getId(), activities);

        //then
        Assertions.assertThat(clubNameAndId.getName()).isEqualTo(response.getBody().getName());
        Assertions.assertThat(clubNameAndId.getId()).isEqualTo(response.getBody().getId());
        List<ActivityImage> savedActivityImgList = em.createQuery("select a from ActivityImage a where a.club.id = :club")
                .setParameter("club", clubNameAndId.getId())
                .getResultList();

        Assertions.assertThat(savedActivityImgList.size()).isEqualTo(actImgCnt);
        savedActivityImgList.stream()
                .forEach(a -> {
                    Assertions.assertThat(a.getClub().getId()).isEqualTo(clubNameAndId.getId());
                    Assertions.assertThat(a.getOriginalName()).isEqualTo(logo.getOriginalFilename());
                });
        List<S3DownloadDto> s3DownloadDtos = s3Transferer.downloadAll(savedActivityImgList.stream().map(FileNames::new).collect(Collectors.toList()));
        for(int i = 0; i < actImgCnt; i++){
            saveImgToResources(s3DownloadDtos.get(i), "createClubTest" + i);
        }
    }

    @Test
    public void appendActImg_BadClubId_ClubIdMisMatchException() throws Exception{
        //given
        Long clubId = -1L;
        List<MultipartFile> activities = new ArrayList<>();
        int actImgCnt = 10;
        for(int i = 0; i < actImgCnt; i++){
            activities.add(getLogoMockMultipartFile());
        }

        //when
        assertThrows(ClubIdMisMatchException.class, () -> clubController.uploadActivityImages(clubId, activities));

        //then

    }
}
