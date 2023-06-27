package com.skklub.admin;

import com.skklub.admin.controller.ClubController;
import com.skklub.admin.controller.dto.ClubCreateRequestDTO;
import com.skklub.admin.controller.dto.ClubNameAndIdDTO;
import com.skklub.admin.controller.dto.ClubResponseDTO;
import com.skklub.admin.domain.Club;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@SpringBootTest
//@Transactional
@Import(TestDataRepository.class)
public class ClubTest {
    @Autowired
    private ClubController clubController;
    @Autowired
    private TestDataRepository testDataRepository;
    
    @Test
    @Order(1)
    public void clubCreate_() throws Exception{
        //given
        Club club = testDataRepository.getCleanClub(0);
        ClubCreateRequestDTO clubCreateRequestDTO = new ClubCreateRequestDTO(
                club.getName(),
                club.getActivityDescription(),
                club.getBriefActivityDescription(),
                club.getClubDescription(),
                club.getBelongs(),
                club.getCampus(),
                club.getClubType(),
                club.getEstablishAt(),
                club.getHeadLine(),
                club.getMandatoryActivatePeriod(),
                club.getMemberAmount(),
                club.getRegularMeetingTime(),
                club.getRoomLocation(),
                club.getWebLink1(),
                club.getWebLink2()
        );
        ClubNameAndIdDTO clubNameAndIdDTO = clubController.createClub(clubCreateRequestDTO, null);

        log.info("clubNameAndIdDTO.getName() : {}", clubNameAndIdDTO.getId());
        log.info("clubNameAndIdDTO.getName() : {}", clubNameAndIdDTO.getName());
        //when
        
        //then
        
    }
    
    @Test
    @Order(2)
    public void readClub() throws Exception{
        //given
        ResponseEntity<ClubResponseDTO> club = clubController.getClubByName("정상적인 클럽 SKKULOL0");
        log.info("club.getBody().getClubDescription() : {}", club.getBody().getClubDescription());
        //when
        
        //then
        
    }
}
