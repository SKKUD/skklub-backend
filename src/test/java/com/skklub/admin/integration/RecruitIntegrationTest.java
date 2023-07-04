package com.skklub.admin.integration;

import com.skklub.admin.TestDataRepository;
import com.skklub.admin.controller.ClubController;
import com.skklub.admin.controller.RecruitController;
import com.skklub.admin.controller.dto.ClubCreateRequestDTO;
import com.skklub.admin.controller.dto.ClubNameAndIdDTO;
import com.skklub.admin.controller.dto.RecruitDto;
import com.skklub.admin.domain.Club;
import com.skklub.admin.domain.Recruit;
import com.skklub.admin.repository.ClubRepository;
import com.skklub.admin.repository.RecruitRepository;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@SpringBootTest
@Transactional
@Import(TestDataRepository.class)
public class RecruitIntegrationTest {
    @Autowired
    private EntityManager em;
    @Autowired
    private TestDataRepository testDataRepository;
    @Autowired
    private RecruitController recruitController;
    @Autowired
    private RecruitRepository recruitRepository;
    @Autowired
    private ClubController clubController;
    @Autowired
    private ClubRepository clubRepository;
    
    @Test
    public void startRecruit_WhenTimeIs() throws Exception{
        //given
        ClubCreateRequestDTO clubCreateRequestDTO = testDataRepository.getClubCreateRequestDTO();
        ClubNameAndIdDTO clubNameAndId = clubController.createClub(clubCreateRequestDTO, null);

        String recruitQuota = "40~50명";
        String recruitProcessDescription = "test Description";
        RecruitDto recruitDto = RecruitDto.builder()
                .recruitStartAt(LocalDateTime.now())
                .recruitEndAt(LocalDateTime.now())
                .recruitQuota(recruitQuota)
                .recruitProcessDescription(recruitProcessDescription)
                .build();
        em.flush();
        em.clear();

        //when
        ClubNameAndIdDTO response = recruitController.startRecruit(clubNameAndId.getId(), recruitDto).getBody();
        em.flush();
        em.clear();

        //then
        Assertions.assertThat(response.getId()).isEqualTo(clubNameAndId.getId());
        Assertions.assertThat(response.getName()).isEqualTo(clubNameAndId.getName());
        Optional<Club> club = clubRepository.findById(clubNameAndId.getId());
        Recruit recruit = club.get().getRecruit();
        Assertions.assertThat(recruit.getId()).isNotNull();
        Assertions.assertThat(recruit.getStartAt()).isEqualTo(recruitDto.getRecruitStartAt());
        Assertions.assertThat(recruit.getEndAt()).isEqualTo(recruitDto.getRecruitEndAt());
        Assertions.assertThat(recruit.getQuota()).isEqualTo(recruitDto.getRecruitQuota());
        Assertions.assertThat(recruit.getProcessDescription()).isEqualTo(recruitDto.getRecruitProcessDescription());
        Assertions.assertThat(recruit.getContact()).isNull();
        Assertions.assertThat(recruit.getWebLink()).isNull();
    }

    @Test
    public void startRecruit_WhenTimeNull() throws Exception{
        //given
        ClubCreateRequestDTO clubCreateRequestDTO = testDataRepository.getClubCreateRequestDTO();
        ClubNameAndIdDTO clubNameAndId = clubController.createClub(clubCreateRequestDTO, null);

        String recruitQuota = "40~50명";
        String recruitProcessDescription = "test Description";
        RecruitDto recruitDto = RecruitDto.builder()
                .recruitQuota(recruitQuota)
                .recruitProcessDescription(recruitProcessDescription)
                .build();
        em.flush();
        em.clear();

        //when
        ClubNameAndIdDTO response = recruitController.startRecruit(clubNameAndId.getId(), recruitDto).getBody();
        em.flush();
        em.clear();

        //then
        Assertions.assertThat(response.getId()).isEqualTo(clubNameAndId.getId());
        Assertions.assertThat(response.getName()).isEqualTo(clubNameAndId.getName());
        Optional<Club> club = clubRepository.findById(clubNameAndId.getId());
        Recruit recruit = club.get().getRecruit();
        Assertions.assertThat(recruit.getId()).isNotNull();
        Assertions.assertThat(recruit.getStartAt()).isNull();
        Assertions.assertThat(recruit.getEndAt()).isNull();
        Assertions.assertThat(recruit.getQuota()).isEqualTo(recruitDto.getRecruitQuota());
        Assertions.assertThat(recruit.getProcessDescription()).isEqualTo(recruitDto.getRecruitProcessDescription());
        Assertions.assertThat(recruit.getContact()).isNull();
        Assertions.assertThat(recruit.getWebLink()).isNull();
    }
    
    @Test
    public void updateRecruit() throws Exception{
        //given
        ClubCreateRequestDTO clubCreateRequestDTO = testDataRepository.getClubCreateRequestDTO();
        ClubNameAndIdDTO clubNameAndId = clubController.createClub(clubCreateRequestDTO, null);

        String recruitQuota = "00명";
        String recruitProcessDescription = "test Description";
        RecruitDto recruitDto = RecruitDto.builder()
                .recruitQuota(recruitQuota)
                .recruitProcessDescription(recruitProcessDescription)
                .build();
        recruitController.startRecruit(clubNameAndId.getId(), recruitDto);
        em.flush();
        em.clear();

        String updateRecruitQuota = "40~50명";
        String updateRecruitProcessDescription = "test Description";
        RecruitDto updateInfo = RecruitDto.builder()
                .recruitStartAt(LocalDateTime.now())
                .recruitEndAt(LocalDateTime.now())
                .recruitQuota(updateRecruitQuota)
                .recruitProcessDescription(updateRecruitProcessDescription)
                .build();
        
        //when

        //then
        
    }

    @Test
    public void endRecruit() throws Exception{
        //given
        ClubCreateRequestDTO clubCreateRequestDTO = testDataRepository.getClubCreateRequestDTO();
        ClubNameAndIdDTO clubNameAndId = clubController.createClub(clubCreateRequestDTO, null);

        String recruitQuota = "40~50명";
        String recruitProcessDescription = "test Description";
        RecruitDto recruitDto = RecruitDto.builder()
                .recruitQuota(recruitQuota)
                .recruitProcessDescription(recruitProcessDescription)
                .build();
        recruitController.startRecruit(clubNameAndId.getId(), recruitDto);
        Optional<Recruit> recruit = recruitRepository.findByClubId(clubNameAndId.getId());
        Assertions.assertThat(recruit).isNotEmpty();
        Long recruitId = recruit.get().getId();
        em.flush();
        em.clear();

        //when
        Long clubId = recruitController.endRecruit(clubNameAndId.getId());
        em.flush();
        em.clear();

        //then
        Assertions.assertThat(clubId).isEqualTo(clubNameAndId.getId());
        Optional<Club> club = clubRepository.findById(clubId);
        Assertions.assertThat(club.get().getRecruit()).isNull();
        Optional<Recruit> recruitShouldEmpty = recruitRepository.findById(recruitId);
        Assertions.assertThat(recruitShouldEmpty).isEmpty();
    }
}
