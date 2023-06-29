package com.skklub.admin;

import com.skklub.admin.controller.ClubController;
import com.skklub.admin.controller.dto.ClubCreateRequestDTO;
import com.skklub.admin.controller.dto.ClubNameAndIdDTO;
import com.skklub.admin.domain.Club;
import com.skklub.admin.domain.enums.Campus;
import com.skklub.admin.domain.enums.ClubType;
import com.skklub.admin.service.ClubService;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;

@Slf4j
@SpringBootTest
@Transactional
public class ClubIntegrationTest {
    @Autowired
    private ClubController clubController;

    @Test
    public void createClub_FullDataWithLogo_() throws Exception{
        //given
        String clubName = "testClubName";
        String activityDescription = "testActivityDescription";
        String briefActivityDescription = "testBriefActivityDescription";
        String clubDescription = "testClubDescription";
        String belongs = "testBelongs";
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
        File logoFile = new File("C:\\Users\\tlgur\\OneDrive\\바탕 화면\\20220820_0247_262.jpg");
        MockMultipartFile logoMultipartFile = new MockMultipartFile("20220820_0247_262.jpg", new FileInputStream(logoFile));

        //when
        ClubNameAndIdDTO clubNameAndIdDTO = clubController.createClub(clubCreateRequestDTO, logoMultipartFile);

        //then
        Assertions.assertThat(clubNameAndIdDTO.getName()).isEqualTo(clubName);

    }
}
