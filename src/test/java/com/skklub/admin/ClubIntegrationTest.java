package com.skklub.admin;

import com.skklub.admin.controller.ClubController;
import com.skklub.admin.controller.S3Transferer;
import com.skklub.admin.controller.dto.ClubCreateRequestDTO;
import com.skklub.admin.controller.dto.ClubNameAndIdDTO;
import com.skklub.admin.controller.dto.S3DownloadDto;
import com.skklub.admin.domain.Club;
import com.skklub.admin.domain.enums.Campus;
import com.skklub.admin.domain.enums.ClubType;
import com.skklub.admin.repository.ClubRepository;
import com.skklub.admin.repository.LogoRepository;
import com.skklub.admin.service.ClubService;
import com.skklub.admin.service.dto.FileNames;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Slf4j
@SpringBootTest
@Transactional
public class ClubIntegrationTest {
    @Autowired
    private ClubController clubController;
    @Autowired
    private S3Transferer s3Transferer;
    @Autowired
    private ClubRepository clubRepository;
    @Autowired
    private LogoRepository logoRepository;

    @Test
    public void createClub_FullDataWithLogo_() throws Exception{
        //given
        String clubName = "testClubName";
        String activityDescription = "testActivityDescription";
        String briefActivityDescription = "testBriefActivityDescription";
        String clubDescription = "testClubDescription";
        String belongs = "평면예술";
        Campus campus = Campus.명륜;
        ClubType clubType = ClubType.중앙동아리;
        Integer establishDate = 1398;
        String headLine = "testHeadLine";
        String mandatoryActivatePeriod = "testMandatoryActivatePeriod";
        Integer memberAmount = 60;
        String regularMeetingTime = "testRegularMeetingTime";
        String roomLocation = "testRoomLocation";
        String webLink1 = "testWebLink1";
        String webLink2 = "testWebLink2";

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
        File logoFile = new File("src/test/resources/img/1.jpg");
        Path path = Paths.get("src/test/resources/img/1.jpg");
        byte[] bytes = Files.readAllBytes(path);
        MockMultipartFile logo = new MockMultipartFile("logo", "1.jpg", "image/jpeg", bytes);

        //when
        ClubNameAndIdDTO clubNameAndIdDTO = clubController.createClub(clubCreateRequestDTO, logo);
        Optional<Club> club = clubRepository.findById(clubNameAndIdDTO.getId());

        //then
        Assertions.assertThat(clubNameAndIdDTO.getName()).isEqualTo(clubName);
        club.ifPresent(c -> {
            Assertions.assertThat(c.getLogo().getOriginalName()).isEqualTo(logo.getOriginalFilename());
            log.info("c.getLogo().getUploadedName() : {}", c.getLogo().getUploadedName());
        });
    }
}
