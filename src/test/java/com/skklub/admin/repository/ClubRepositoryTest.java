package com.skklub.admin.repository;

import com.skklub.admin.domain.*;
import com.skklub.admin.domain.enums.Campus;
import com.skklub.admin.domain.enums.ClubType;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Transactional
@SpringBootTest
class ClubRepositoryTest {
    @Autowired
    private ClubRepository clubRepository;
    @Autowired
    private EntityManager em;

    @BeforeEach
    public void beforeEach() {

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
        club.matchLogo(logo);
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
}
