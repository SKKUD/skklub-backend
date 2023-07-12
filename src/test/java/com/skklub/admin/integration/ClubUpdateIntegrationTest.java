package com.skklub.admin.integration;

import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.skklub.admin.TestDataRepository;
import com.skklub.admin.controller.ClubController;
import com.skklub.admin.controller.S3Transferer;
import com.skklub.admin.controller.dto.ClubCreateRequestDTO;
import com.skklub.admin.controller.dto.ClubIdAndLogoNameDTO;
import com.skklub.admin.controller.dto.ClubNameAndIdDTO;
import com.skklub.admin.domain.Club;
import com.skklub.admin.domain.Logo;
import com.skklub.admin.domain.enums.Campus;
import com.skklub.admin.domain.enums.ClubType;
import com.skklub.admin.repository.ClubRepository;
import com.skklub.admin.service.dto.FileNames;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
@Transactional
@SpringBootTest
@Import(TestDataRepository.class)
public class ClubUpdateIntegrationTest {
    @Autowired
    private TestDataRepository testDataRepository;
    @Autowired
    private ClubController clubController;
    @Autowired
    private ClubRepository clubRepository;
    @Autowired
    private EntityManager em;
    @Autowired
    private S3Transferer s3Transferer;


    @Test
    public void updateClub_GivenClub_CheckUpdatedInfo() throws Exception{
        //given
        ClubCreateRequestDTO clubCreateRequestDTO = testDataRepository.getClubCreateRequestDTO();
        ClubNameAndIdDTO clubNameAndIdDTO = clubController.createClub(clubCreateRequestDTO, null);

        String clubName = "changedClubName";
        String activityDescription = "changedActivityDescription";
        String briefActivityDescription = "changedBriefActivityDescription";
        String clubDescription = "changedClubDescription";
        String belongs = "평면예술";
        Campus campus = Campus.명륜;
        ClubType clubType = ClubType.중앙동아리;
        Integer establishDate = 1398;
        String headLine = "changedHeadLine";
        String mandatoryActivatePeriod = "changedMandatoryActivatePeriod";
        Integer memberAmount = 40;
        String regularMeetingTime = "changedRegularMeetingTime";
        String roomLocation = "changedRoomLocation";
        String webLink1 = "changedWebLink1";
        String webLink2 = "changedWebLink2";

        ClubCreateRequestDTO clubChangeInfo = ClubCreateRequestDTO.builder()
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

        //when
        ClubNameAndIdDTO changedClubNameAndId = clubController.updateClub(clubNameAndIdDTO.getId(), clubChangeInfo).getBody();

        //then
        Assertions.assertThat(changedClubNameAndId.getId()).isEqualTo(clubNameAndIdDTO.getId());
        Optional<Club> club = clubRepository.findById(changedClubNameAndId.getId());
        Assertions.assertThat(club).isNotEmpty();
        club.ifPresent(
                c ->{
                    Assertions.assertThat(c.getId()).isNotNull();
                    Assertions.assertThat(c.getName()).isEqualTo(clubChangeInfo.getClubName());
                    Assertions.assertThat(c.getActivityDescription()).isEqualTo(clubChangeInfo.getActivityDescription());
                    Assertions.assertThat(c.getBriefActivityDescription()).isEqualTo(clubChangeInfo.getBriefActivityDescription());
                    Assertions.assertThat(c.getClubDescription()).isEqualTo(clubChangeInfo.getClubDescription());
                    Assertions.assertThat(c.getBelongs()).isEqualTo(clubChangeInfo.getBelongs());
                    Assertions.assertThat(c.getCampus()).isEqualTo(clubChangeInfo.getCampus());
                    Assertions.assertThat(c.getClubType()).isEqualTo(clubChangeInfo.getClubType());
                    Assertions.assertThat(c.getEstablishAt()).isEqualTo(clubChangeInfo.getEstablishDate());
                    Assertions.assertThat(c.getHeadLine()).isEqualTo(clubChangeInfo.getHeadLine());
                    Assertions.assertThat(c.getMandatoryActivatePeriod()).isEqualTo(clubChangeInfo.getMandatoryActivatePeriod());
                    Assertions.assertThat(c.getMemberAmount()).isEqualTo(clubChangeInfo.getMemberAmount());
                    Assertions.assertThat(c.getRegularMeetingTime()).isEqualTo(clubChangeInfo.getRegularMeetingTime());
                    Assertions.assertThat(c.getRoomLocation()).isEqualTo(clubChangeInfo.getRoomLocation());
                    Assertions.assertThat(c.getWebLink1()).isEqualTo(clubChangeInfo.getWebLink1());
                    Assertions.assertThat(c.getWebLink2()).isEqualTo(clubChangeInfo.getWebLink2());
                }
        );

    }
    
    @Test
    public void updateLogo_GivenSomeLogo_DeleteOldFromS3() throws Exception{
        //given
        ClubCreateRequestDTO clubCreateRequestDTO = testDataRepository.getClubCreateRequestDTO();

        Path oldPath = Paths.get("src/test/resources/img/1.jpg");
        byte[] oldBytes = Files.readAllBytes(oldPath);
        String oldLogoName = "1.jpg";
        MockMultipartFile oldLogoFile = new MockMultipartFile("logo", oldLogoName, "image/jpeg", oldBytes);
        ClubNameAndIdDTO nameAndId = clubController.createClub(clubCreateRequestDTO, oldLogoFile);

        List oldLogo = em.createQuery("select l from Logo l where l.originalName = :name")
                .setParameter("name", oldLogoName)
                .getResultList();

        Path newPath = Paths.get("src/test/resources/img/2.jpg");
        byte[] newBytes = Files.readAllBytes(newPath);
        String newLogoName = "2.jpg";
        MockMultipartFile newLogo = new MockMultipartFile("logo", newLogoName, "image/jpeg", newBytes);
        em.flush();
        em.clear();

        //when
        ClubIdAndLogoNameDTO clubIdAndLogoNameDTO = clubController.updateLogo(nameAndId.getId(), newLogo).getBody();

        //then
        List oldLogoResult = em.createQuery("select l from Logo l where l.originalName = :name")
                .setParameter("name", oldLogoName)
                .getResultList();
        Assertions.assertThat(oldLogoResult).isEmpty();
        Optional<Club> findedClub = clubRepository.findById(clubIdAndLogoNameDTO.getClubId());
        Assertions.assertThat(findedClub).isNotEmpty();
        Assertions.assertThat(findedClub.get().getLogo().getOriginalName())
                .isEqualTo(newLogoName)
                .isEqualTo(clubIdAndLogoNameDTO.getLogoOriginalName());

        FileNames fileName = new FileNames((Logo) oldLogo.get(0));
        Assertions.assertThat(fileName.getId()).isNotNull();
        Assertions.assertThat(fileName.getOriginalName()).isEqualTo(oldLogoName);
        assertThrows(AmazonS3Exception.class, () -> {
            s3Transferer.downloadOne(fileName);
        });

    }

    @Test
    public void updateLogo_FromDefaultLogo_DeleteOldFromS3() throws Exception{
        //given
        ClubCreateRequestDTO clubCreateRequestDTO = testDataRepository.getClubCreateRequestDTO();

        ClubNameAndIdDTO nameAndId = clubController.createClub(clubCreateRequestDTO, null);

        Path newPath = Paths.get("src/test/resources/img/2.jpg");
        byte[] newBytes = Files.readAllBytes(newPath);
        String newLogoName = "2.jpg";
        MockMultipartFile newLogo = new MockMultipartFile("logo", newLogoName, "image/jpeg", newBytes);
        Long defaultLogoCount = em.createQuery("select count(l) from Logo l where l.originalName = :name", Long.class)
                .setParameter("name", "alt.jpg")
                .getSingleResult();
        em.flush();
        em.clear();

        //when
        ClubIdAndLogoNameDTO clubIdAndLogoNameDTO = clubController.updateLogo(nameAndId.getId(), newLogo).getBody();

        //then
        Long afterUpdateDefaultLogoCount = em.createQuery("select count(l) from Logo l where l.originalName = :name", Long.class)
                .setParameter("name", "alt.jpg")
                .getSingleResult();
        Assertions.assertThat(afterUpdateDefaultLogoCount).isEqualTo(defaultLogoCount - 1);
        Optional<Club> findedClub = clubRepository.findById(clubIdAndLogoNameDTO.getClubId());
        Assertions.assertThat(findedClub).isNotEmpty();
        Assertions.assertThat(findedClub.get().getLogo().getOriginalName())
                .isEqualTo(newLogoName)
                .isEqualTo(clubIdAndLogoNameDTO.getLogoOriginalName());

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(
                () -> s3Transferer.downloadOne(new FileNames("alt.jpg", "alt.jpg")));


    }

}
