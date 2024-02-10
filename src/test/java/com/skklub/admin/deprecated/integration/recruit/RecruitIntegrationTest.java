package com.skklub.admin.deprecated.integration.recruit;

import com.skklub.admin.TestDataRepository;
import com.skklub.admin.WithMockCustomUser;
import com.skklub.admin.controller.ClubController;
import com.skklub.admin.controller.RecruitController;
import com.skklub.admin.controller.dto.ClubNameAndIdDTO;
import com.skklub.admin.controller.dto.RecruitRequest;
import com.skklub.admin.domain.Club;
import com.skklub.admin.domain.Recruit;
import com.skklub.admin.domain.enums.Role;
import com.skklub.admin.repository.ClubRepository;
import com.skklub.admin.repository.RecruitRepository;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@SpringBootTest
@Transactional
@Import(TestDataRepository.class)
@WithMockCustomUser(username = "testMasterID",role = Role.ROLE_MASTER)
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
        Club club = em.createQuery("select c from Club c where c.president is not null ", Club.class)
                .setMaxResults(1)
                .getSingleResult();

        RecruitRequest recruitRequest = readyRecruitReqFullTime();
        em.flush();
        em.clear();

        //when
        ClubNameAndIdDTO response = recruitController.startRecruit(club.getId(), recruitRequest).getBody();
        em.flush();
        em.clear();

        //then
        Assertions.assertThat(response.getId()).isEqualTo(club.getId());
        Assertions.assertThat(response.getName()).isEqualTo(club.getName());
        Optional<Club> clubAfterRecruit = clubRepository.findById(club.getId());
        Recruit recruit = clubAfterRecruit.get().getRecruit();
        Assertions.assertThat(recruit.getId()).isNotNull();
        Assertions.assertThat(recruit.getQuota()).isEqualTo(recruitRequest.getRecruitQuota());
        Assertions.assertThat(recruit.getProcessDescription()).isEqualTo(recruitRequest.getRecruitProcessDescription());
        Assertions.assertThat(recruit.getContact()).isEqualTo(recruitRequest.getRecruitContact());
        Assertions.assertThat(recruit.getWebLink()).isEqualTo(recruitRequest.getRecruitWebLink());
    }

    @Test
    public void startRecruit_WhenTimeNull() throws Exception{
        //given
        Club club = em.createQuery("select c from Club c where c.president is not null ", Club.class)
                .setMaxResults(1)
                .getSingleResult();

        RecruitRequest recruitRequest = readyRecruitReqNoTime();
        em.flush();
        em.clear();

        //when
        ClubNameAndIdDTO response = recruitController.startRecruit(club.getId(), recruitRequest).getBody();
        em.flush();
        em.clear();

        //then
        Assertions.assertThat(response.getId()).isEqualTo(club.getId());
        Assertions.assertThat(response.getName()).isEqualTo(club.getName());
        Optional<Club> clubAfterRecruit = clubRepository.findById(club.getId());
        Recruit recruit = clubAfterRecruit.get().getRecruit();
        Assertions.assertThat(recruit.getId()).isNotNull();
        Assertions.assertThat(recruit.getStartAt()).isNull();
        Assertions.assertThat(recruit.getEndAt()).isNull();
        Assertions.assertThat(recruit.getQuota()).isEqualTo(recruitRequest.getRecruitQuota());
        Assertions.assertThat(recruit.getProcessDescription()).isEqualTo(recruitRequest.getRecruitProcessDescription());
        Assertions.assertThat(recruit.getContact()).isEqualTo(recruitRequest.getRecruitContact());
        Assertions.assertThat(recruit.getWebLink()).isEqualTo(recruitRequest.getRecruitWebLink());
    }
    
    @Test
    public void updateRecruit() throws Exception{
        //given
        Club club = em.createQuery("select c from Club c inner join fetch c.recruit r where c.recruit is not null", Club.class)
                .setMaxResults(1)
                .getSingleResult();
        em.clear();

        RecruitRequest recruitRequest = readyRecruitReqFullTime();

        //when
        ResponseEntity<Long> response = recruitController.updateRecruit(club.getId(), recruitRequest);
        em.flush();
        em.clear();

        //then
        Assertions.assertThat(response.getBody()).isEqualTo(club.getId());
        Recruit recruitAfterUpdate = em.createQuery("select r from Club c inner join c.recruit r where c.id = :clubId", Recruit.class)
                .setParameter("clubId", club.getId())
                .getSingleResult();
        Assertions.assertThat(recruitAfterUpdate.getContact()).isEqualTo(recruitRequest.getRecruitContact());
        Assertions.assertThat(recruitAfterUpdate.getWebLink()).isEqualTo(recruitRequest.getRecruitWebLink());
        Assertions.assertThat(recruitAfterUpdate.getProcessDescription()).isEqualTo(recruitRequest.getRecruitProcessDescription());
        Assertions.assertThat(recruitAfterUpdate.getQuota()).isEqualTo(recruitRequest.getRecruitQuota());
    }

    @Test
    public void endRecruit() throws Exception{
        //given
        Club club = em.createQuery("select c from Club c inner join fetch c.recruit r where c.recruit is not null", Club.class)
                .setMaxResults(1)
                .getSingleResult();
        Recruit recruit = club.getRecruit();
        em.clear();

        //when
        Long clubId = recruitController.endRecruit(club.getId());
        em.flush();
        em.clear();

        //then
        Assertions.assertThat(clubId).isEqualTo(club.getId());
        Optional<Club> clubAfterRecruitEnd = clubRepository.findById(clubId);
        Assertions.assertThat(clubAfterRecruitEnd.get().getRecruit()).isNull();
        Optional<Recruit> recruitShouldEmpty = recruitRepository.findById(recruit.getId());
        Assertions.assertThat(recruitShouldEmpty).isEmpty();
    }

    private RecruitRequest readyRecruitReqFullTime() {
        return RecruitRequest.builder()
                .recruitStartAt(LocalDateTime.now())
                .recruitEndAt(LocalDateTime.now())
                .recruitQuota("00명 || 최대한 많이 뽑을 예정")
                .recruitProcessDescription("1. 어쩌구 2. 어쩌구 AnyString")
                .recruitContact("010 - 1234 - 1234 || 인스타 아이디")
                .recruitWebLink("www.xxx.com || or any String")
                .build();
    }

    private RecruitRequest readyRecruitReqNoTime() {
        return RecruitRequest.builder()
                .recruitQuota("00명 || 최대한 많이 뽑을 예정")
                .recruitProcessDescription("1. 어쩌구 2. 어쩌구 AnyString")
                .recruitContact("010 - 1234 - 1234 || 인스타 아이디")
                .recruitWebLink("www.xxx.com || or any String")
                .build();
    }
}
