package com.skklub.admin.repository;

import com.skklub.admin.TestDataRepository;
import com.skklub.admin.domain.ActivityImage;
import com.skklub.admin.domain.Club;
import com.skklub.admin.domain.Logo;
import com.skklub.admin.domain.Recruit;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Test Class for Checking foreign Relation Working
 */
@Slf4j
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestDataRepository.class)
public class DomainRepositoryTest {
    @Autowired
    private EntityManager em;
    @Autowired
    private TestDataRepository testDataRepository;
    @Autowired
    private ClubRepository clubRepository;
    @Autowired
    private ActivityImageRepository activityImageRepository;
    @Autowired
    private LogoRepository logoRepository;
    @Autowired
    private RecruitRepository recruitRepository;

    @BeforeEach
    public void beforeEach(){
        testDataRepository = new TestDataRepository();
    }

    @Test
    public void saveTest_WithNoRelation_getNewId() throws Exception{
        //given
        Club club = testDataRepository.getCleanClub(0);
        Logo logo = testDataRepository.getLogos().get(0);
        List<ActivityImage> activityImages = testDataRepository.getActivityImages(0);
        Recruit recruit = testDataRepository.getRecruits().get(0);

        //when
        Assertions.assertThat(club.getId()).isNull();
        Assertions.assertThat(logo.getId()).isNull();
        Assertions.assertThat(recruit.getId()).isNull();
        activityImages.stream().forEach(
                img -> Assertions.assertThat(img.getId()).isNull()
        );
        Club savedClub = clubRepository.save(club); // 1 Query
        Logo savedLogo = logoRepository.save(logo); // 1 Query
        List<ActivityImage> savedImgs = activityImageRepository.saveAll(activityImages); // 5 Query
        Recruit savedRecruit = recruitRepository.save(recruit); // 1 Query
        em.flush();
        em.clear();

        //then
        Assertions.assertThat(savedClub.getId()).isNotNull();
        Assertions.assertThat(savedLogo.getId()).isNotNull();
        Assertions.assertThat(savedRecruit.getId()).isNotNull();
        savedImgs.stream().forEach(
                img -> Assertions.assertThat(img.getId()).isNotNull()
        );
    }
    
    @Test
    public void clubSaveAndFind_GivenLogoAndActImgAndRecruit_LazyAndSameEntity() throws Exception{
        //given
        Club club = testDataRepository.getCleanClub(0);
        Logo logo = testDataRepository.getLogos().get(0);
        List<ActivityImage> activityImages = testDataRepository.getActivityImages(0);
        Recruit recruit = testDataRepository.getRecruits().get(0);

        //when
        club.changeLogo(logo); //1 Insert
        club.appendActivityImages(activityImages);
        club.startRecruit(recruit);
        recruitRepository.save(recruit);
        clubRepository.save(club); // 1 Insert
        activityImageRepository.saveAll(activityImages);
        em.flush();
        em.clear();
        log.info("after Insert====================");
        Optional<Club> findedClub = clubRepository.findById(club.getId());
        log.info("find Club=================");

        //then
        Assertions.assertThat(findedClub).isNotEmpty();
        findedClub.ifPresent(
                c ->{
                    Assertions.assertThat(c.getLogo().getOriginalName()).isEqualTo(logo.getOriginalName());
                    log.info("find Logo=================");
                    Assertions.assertThat(c.getRecruit().getContact()).isEqualTo(recruit.getContact());
                    log.info("find Recruit=================");
                    Assertions.assertThat(c.getActivityImages().size()).isEqualTo(activityImages.size());
                    c.getActivityImages().stream().forEach(
                            img -> Assertions.assertThat(img.getClub()).isEqualTo(c)
                    );
                    log.info("find Imgs=================");
                }
        );
    }

    @Test
    public void saveActImg_GivenClub() throws Exception{
        //given
        Club club = testDataRepository.getCleanClub(0);
        clubRepository.save(club);
        club.getActivityImages().clear();
        List<ActivityImage> activityImages = testDataRepository.getActivityImages(0);
        em.flush();
        em.clear();

        //when
        // 5 Insert
        club.appendActivityImages(activityImages);
        activityImageRepository.saveAll(activityImages);
        em.flush();
        em.clear();
        Optional<Club> findedClub = clubRepository.findById(club.getId());
        List<ActivityImage> findedImgs = activityImageRepository.findAllById(activityImages.stream().map(ActivityImage::getId).collect(Collectors.toList()));

        //then
        findedImgs.stream().forEach(
                img -> Assertions.assertThat(img.getClub()).isEqualTo(club)
        );
        log.info("==============");
        findedClub.ifPresent(
                c -> {
                    Assertions.assertThat(c.getActivityImages().size()).isEqualTo(activityImages.size());
                }
        );
    }

    @Test
    public void updateRecruit_MappedToClub_ChangedInfoReadFromClub() throws Exception{
        //given
        Club club = testDataRepository.getCleanClub(0);
        Recruit recruit = testDataRepository.getRecruits().get(0);
        Recruit recruitUpdateInfo = testDataRepository.getRecruits().get(1);
        club.startRecruit(recruit);
        // 2 Insert
        recruitRepository.save(recruit);
        clubRepository.save(club);
        em.flush();
        em.clear();

        //when
        //1 Select, 1 Update
        Optional<Recruit> findedRecruit = recruitRepository.findById(recruit.getId());
        findedRecruit.ifPresent(
                r -> r.update(recruitUpdateInfo)
        );
        em.flush();
        em.clear();
        // 1 Select
        Optional<Club> findedClub = clubRepository.findById(club.getId());

        //then
        Field idField = recruitUpdateInfo.getClass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(recruitUpdateInfo, findedRecruit.get().getId());
        findedClub.ifPresent(
                c -> {
                    // 1 Select
                    Assertions.assertThat(c.getRecruit()).isEqualTo(findedRecruit.get());
                    Assertions.assertThat(c.getRecruit()).isEqualTo(recruitUpdateInfo);
                    Assertions.assertThat(c.getRecruit().getId()).isEqualTo(recruit.getId());
                }
        );
    }

    @Test
    public void updateLogo_MappedToClub_ChangedInfoReadFromClub() throws Exception{
        //given
        Club club = testDataRepository.getCleanClub(0);
        Logo logo = testDataRepository.getLogos().get(0);
        Logo logoUpdateInfo = testDataRepository.getLogos().get(1);
        club.changeLogo(logo);
        // 2 Insert
        logoRepository.save(logo);
        clubRepository.save(club);
        em.flush();
        em.clear();

        //when
        //1 Select, 1 Update
        Optional<Logo> findedLogo = logoRepository.findById(logo.getId());
        findedLogo.ifPresent(
                l -> l.update(logoUpdateInfo)
        );
        em.flush();
        em.clear();
        // 1 Select
        Optional<Club> findedClub = clubRepository.findById(club.getId());

        //then
        Field idField = logoUpdateInfo.getClass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(logoUpdateInfo, findedLogo.get().getId());
        findedClub.ifPresent(
                c -> {
                    // 1 Select
                    Assertions.assertThat(c.getLogo()).isEqualTo(findedLogo.get());
                    Assertions.assertThat(c.getLogo()).isEqualTo(logoUpdateInfo);
                    Assertions.assertThat(c.getLogo().getId()).isEqualTo(logo.getId());
                }
        );
    }

    @Test
    public void deleteRecruit_GivenClub_CannotFindFromClub() throws Exception{
        //given
        Club club = testDataRepository.getCleanClub(0);
        Recruit recruit = testDataRepository.getRecruits().get(0);
        //4 Insert
        club.startRecruit(recruit);
        recruitRepository.save(recruit);
        clubRepository.save(club);
        em.flush();
        em.clear();

        //when
        //1 Select 1 Delete
        Optional<Club> findedClub = clubRepository.findById(club.getId());
        findedClub.ifPresent(
                c -> c.endRecruit()
        );
        em.flush();
        em.clear();

        //then
        findedClub = clubRepository.findById(club.getId());
        findedClub.ifPresent(
                c -> {
                    Assertions.assertThat(c.getRecruit()).isNull();
                }
        );
        Assertions.assertThat(recruitRepository.findById(recruit.getId())).isEmpty();

    }

    @Test
    public void deleteActImg_GivenClub_CannotFindFromClub() throws Exception{
        //given
        Club club = testDataRepository.getCleanClub(0);
        List<ActivityImage> activityImages = testDataRepository.getActivityImages(0);
        //4 Insert
        clubRepository.save(club);
        club.appendActivityImages(activityImages);
        activityImageRepository.saveAll(activityImages);
        em.flush();
        em.clear();

        //when
        //1 Select, 1 Delete
        Optional<ActivityImage> findedActImg = activityImageRepository.findByClubIdAndOriginalName(club.getId(), activityImages.get(1).getOriginalName());
        findedActImg.ifPresent(activityImageRepository::delete);
        em.flush();
        em.clear();

        //then
        //1 Select
        Optional<Club> findedClub = clubRepository.findById(club.getId());
        findedClub.ifPresent(
                c ->{
                    Assertions.assertThat(c.getActivityImages().size()).isEqualTo(activityImages.size() - 1);
                    List<ActivityImage> clubImgs = c.getActivityImages().stream()
                            .filter(img -> img.getOriginalName().equals(findedActImg.get().getOriginalName()))
                            .collect(Collectors.toList());
                    //1 Select
                    Assertions.assertThat(clubImgs).isEmpty();
                }
        );
    }

    @Test
    public void clubReviveRemove_GivenClub_() throws Exception{
        //given
        Club club = testDataRepository.getCleanClub(0);
        clubRepository.save(club);
        em.flush();
        em.clear();

        //when
        Optional<Club> findedClub = clubRepository.findById(club.getId());
        findedClub.ifPresent(Club::remove);
        //1 Update
        em.flush();
        findedClub.ifPresent(Club::revive);
        //1 Update
        em.flush();


        //then

    }
}
