package com.skklub.admin.repository;

import com.skklub.admin.domain.*;
import com.skklub.admin.domain.enums.Campus;
import com.skklub.admin.domain.enums.ClubType;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Transactional
@SpringBootTest
@Slf4j
class ClubRepositoryTest {
    @Autowired
    private ClubRepository clubRepository;
    @Autowired
    private EntityManager em;

    @BeforeEach
    public void beforeEach() {
        for(int t = 0; t < 100; t++) {
            Club club = new Club(
                    "정상적인 클럽 SKKULOL" + t
                    , "1. 열심히 참여하면 됩니다 2. 그냥 게임만 잘 하면 됩니다."
                    , "취미교양"
                    , ClubType.중앙동아리
                    , "E-SPORTS"
                    , Campus.명륜
                    , "여기가 어떤 동아리냐면요, 페이커가 될 수 있게 해주는 동아리입니다^^"
                    , "2023"
                    , "명륜 게임 동아리입니다"
                    , "4학기"
                    , 60
                    , "Thursday 19:00"
                    , "학생회관 80210"
                    , "www.skklol.com"
                    , "www.skkulol.edu");
            Recruit recruit = new Recruit(LocalDateTime.now(), LocalDateTime.now(), "20명", "1차 서류, 2차 면접", "010 - 0000 - 0000", "www.example.com");
            User user = new User("exampleId", "examplePw", 3, "Lee", "010 - 1234 - 5678");
            Logo logo = new Logo("logoFile.png" + t, "uploadedLogo.png");
            List<ActivityImage> activityImages = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                ActivityImage a = new ActivityImage("activityImages" + i + ".png", "uploadedActivityImage" + i + ".png");
                em.persist(a);
                activityImages.add(a);
            }

            club.appendActivityImages(activityImages);
            club.changeLogo(logo);
            club.startRecruit(recruit);
            club.setUser(user);

            em.persist(recruit);
            em.persist(user);
            em.persist(club);
            em.persist(logo);
        }

        em.flush();
        em.clear();
    }

    @Test
    public void findDetailClubInfoById_Default_Success() throws Exception{
        //given
        Club club = new Club(
                "정상적인 클럽 SKKULOL"
                , "1. 열심히 참여하면 됩니다 2. 그냥 게임만 잘 하면 됩니다."
                , "취미교양"
                , ClubType.중앙동아리
                , "E-SPORTS"
                , Campus.명륜
                , "여기가 어떤 동아리냐면요, 페이커가 될 수 있게 해주는 동아리입니다^^"
                , "2023"
                , "명륜 게임 동아리입니다"
                , "4학기"
                , 60
                , "Thursday 19:00"
                , "학생회관 80210"
                , "www.skklol.com"
                , "www.skkulol.edu");
        Recruit recruit = new Recruit(LocalDateTime.now(), LocalDateTime.now(), "20명", "1차 서류, 2차 면접", "010 - 0000 - 0000", "www.example.com");
        User user = new User("exampleId", "examplePw", 3, "Lee", "010 - 1234 - 5678");
        Logo logo = new Logo("logoFile.png", "uploadedLogo.png");
        List<ActivityImage> activityImages = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            ActivityImage a = new ActivityImage("activityImages" + i + ".png", "uploadedActivityImage" + i + ".png");
            em.persist(a);
            activityImages.add(a);
        }

        club.appendActivityImages(activityImages);
        club.changeLogo(logo);
        club.startRecruit(recruit);
        club.setUser(user);

        em.persist(recruit);
        em.persist(user);
        em.persist(club);
        em.persist(logo);
        Long clubId = club.getId();
        Long recruitId = recruit.getId();
        Long userId = user.getId();
        Long logoId = logo.getId();
        List<Long> activityImagesId = activityImages.stream()
                .map(a -> a.getId())
                .collect(Collectors.toList());

        em.flush();
        em.clear();

        //when

        Club findedClub = clubRepository.findDetailClubById(clubId).get();

        //then

        Assertions.assertThat(findedClub.getRecruit().getId()).isEqualTo(recruitId);
        Assertions.assertThat(findedClub.getPresident().getId()).isEqualTo(userId);
        Assertions.assertThat(findedClub.getLogo().getId()).isEqualTo(logoId);
        Assertions.assertThat(findedClub.getActivityImages().size()).isEqualTo(10);
        for (int i = 0; i < 10; i++) {
            Assertions.assertThat(findedClub.getActivityImages().get(i).getId()).isEqualTo(activityImagesId.get(i));
        }
     }

    @Test
    public void findDetailClubInfoById_RecruitNull_HasNull() throws Exception{
        //given
        Club club = new Club(
                "정상적인 클럽 SKKULOL"
                , "1. 열심히 참여하면 됩니다 2. 그냥 게임만 잘 하면 됩니다."
                , "취미교양"
                , ClubType.중앙동아리
                , "E-SPORTS"
                , Campus.명륜
                , "여기가 어떤 동아리냐면요, 페이커가 될 수 있게 해주는 동아리입니다^^"
                , "2023"
                , "명륜 게임 동아리입니다"
                , "4학기"
                , 60
                , "Thursday 19:00"
                , "학생회관 80210"
                , "www.skklol.com"
                , "www.skkulol.edu");
        Recruit recruit = new Recruit(LocalDateTime.now(), LocalDateTime.now(), "20명", "1차 서류, 2차 면접", "010 - 0000 - 0000", "www.example.com");
        User user = new User("exampleId", "examplePw", 3, "Lee", "010 - 1234 - 5678");
        club.setUser(user);

        em.persist(recruit);
        em.persist(user);
        em.persist(club);
        Long clubId = club.getId();
        Long recruitId = recruit.getId();
        Long userId = user.getId();

        em.flush();
        em.clear();

        //when

        Club findedClub = clubRepository.findDetailClubById(clubId).get();

        //then

        org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> findedClub.getRecruit().getId());
        Assertions.assertThat(findedClub.getPresident().getId()).isEqualTo(userId);
    }

    @Test
    public void findClubPrevs_FullData_Success() throws Exception{
        //given
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.Direction.ASC, "id");

        //when
        Page<Club> clubPrevs = clubRepository.findClubPrevByCampusAndClubTypeAndBelongsOrderByName(Campus.명륜, ClubType.중앙동아리, "취미교양", pageRequest);

        //then
        Assertions.assertThat(clubPrevs.getTotalElements()).isEqualTo(10);
        Assertions.assertThat(clubPrevs.getTotalPages()).isEqualTo(4);
        Assertions.assertThat(clubPrevs.getNumber()).isEqualTo(0);
        Assertions.assertThat(clubPrevs.getSize()).isEqualTo(3);
        Assertions.assertThat(clubPrevs.isFirst()).isTrue();
        Assertions.assertThat(clubPrevs.hasNext()).isTrue();
     }

     @Test
     public void findClubPrevs_withNullParameters_NoResult() throws Exception{
         //given
         PageRequest pageRequest = PageRequest.of(0, 10, Sort.Direction.ASC, "id");

         //when
         Page<Club> clubPrevs = clubRepository.findClubPrevByCampusAndClubTypeAndBelongsOrderByName(Campus.명륜, null, "취미교양", pageRequest);
         //then
         Assertions.assertThat(clubPrevs.getContent().size()).isZero();
         Assertions.assertThat(clubPrevs.isFirst()).isTrue();
         Assertions.assertThat(clubPrevs.hasNext()).isFalse();
      }
      
      @Test
      public void findClubRandomByCategories_try3TimesWithFullData_DifferentContent() throws Exception {
          //given
          String campus ="명륜";
          String clubType = "중앙동아리";
          String belongs = "취미교양";
          //when
          List<Club> randoms1 = clubRepository.findClubRandomByCategories(campus, clubType, belongs);
          List<Club> randoms2 = clubRepository.findClubRandomByCategories(campus, clubType, belongs);
          List<Club> randoms3 = clubRepository.findClubRandomByCategories(campus, clubType, belongs);
          //then
          Assertions.assertThat(randoms1.size()).isEqualTo(3);
          Assertions.assertThat(randoms2.size()).isEqualTo(3);
          Assertions.assertThat(randoms3.size()).isEqualTo(3);
          for (int i = 0; i < 3; i++) {
              log.info("random1 : {} {}", randoms1.get(i).getId(), randoms1.get(i).getName());
              log.info("random2 : {} {}", randoms2.get(i).getId(), randoms2.get(i).getName());
              log.info("random3 : {} {}", randoms3.get(i).getId(), randoms3.get(i).getName());
              Assertions.assertThat(randoms1.get(i)).isNotEqualTo(randoms2.get(i));
              Assertions.assertThat(randoms2.get(i)).isNotEqualTo(randoms3.get(i));
              Assertions.assertThat(randoms3.get(i)).isNotEqualTo(randoms1.get(i));
          }

      }
}
