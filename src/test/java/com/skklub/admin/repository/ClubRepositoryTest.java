package com.skklub.admin.repository;

import com.skklub.admin.TestDataRepository;
import com.skklub.admin.domain.imagefile.ActivityImage;
import com.skklub.admin.domain.Club;
import com.skklub.admin.domain.imagefile.Logo;
import com.skklub.admin.domain.Recruit;
import com.skklub.admin.domain.enums.Campus;
import com.skklub.admin.domain.enums.ClubType;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestDataRepository.class)
class ClubRepositoryTest {
    @Autowired
    private ClubRepository clubRepository;
    @Autowired
    private ActivityImageRepository activityImageRepository;
    @Autowired
    private RecruitRepository recruitRepository;
    @Autowired
    private EntityManager em;
    @Autowired
    private TestDataRepository testDataRepository;

    @BeforeEach
    public void beforeEach() {
        testDataRepository = new TestDataRepository();
    }

    @Test
    public void save_Default_checkLogoCascade() throws Exception {
        //given
        Club club = testDataRepository.getCleanClub(0);
        Logo logo = testDataRepository.getLogos().get(0);
        Assertions.assertThat(club.getId()).isNull();
        Assertions.assertThat(club.getLogo()).isNull();
        Assertions.assertThat(logo.getId()).isNull();

        //when
        club.changeLogo(logo);
        clubRepository.save(club);

        //then
        Assertions.assertThat(club.getId()).isNotNull();
        Assertions.assertThat(logo.getId()).isNotNull();
        Assertions.assertThat(club.getLogo()).isNotNull();
        Assertions.assertThat(club.getLogo()).isEqualTo(logo);
    }

    @Test
    public void findById_Default_LazyLoading() throws Exception {
        //given
        Club club = testDataRepository.getCleanClub(0);
        Logo logo = testDataRepository.getLogos().get(0);
        Assertions.assertThat(club.getId()).isNull();
        Assertions.assertThat(club.getActivityImages()).isEmpty();
        club.changeLogo(logo);
        clubRepository.save(club);
        em.flush();
        em.clear();

        //when
        Optional<Club> findClub = clubRepository.findById(club.getId());

        //then
        Assertions.assertThat(findClub).isNotEmpty();
        Assertions.assertThat(findClub.get()).isEqualTo(club);
        em.clear();
        assertThrows(LazyInitializationException.class,
                () -> findClub.get().getLogo().getOriginalName());
        assertThrows(NullPointerException.class,
                () -> findClub.get().getPresident().getContact());
        assertThrows(LazyInitializationException.class,
                () -> findClub.get().getActivityImages().size());
        assertThrows(NullPointerException.class,
                () -> findClub.get().getRecruit().getContact());
    }

    @Test
    public void saveAll_Default_checkRelation() throws Exception {
        //given
        Club club = testDataRepository.getCleanClub(0);
        Assertions.assertThat(club.getId()).isNull();
        Assertions.assertThat(club.getActivityImages()).isEmpty();
        clubRepository.save(club);
        List<ActivityImage> activityImages = testDataRepository.getActivityImages(3);

        //when
        Optional<Club> findClub = clubRepository.findById(club.getId());
        findClub.ifPresent(
                c -> {
                    c.appendActivityImages(activityImages);
                    activityImageRepository.saveAll(activityImages);
                }
        );
        em.flush();
        em.clear();

        //then
        List<ActivityImage> clubActivityImages = club.getActivityImages();
        Assertions.assertThat(clubActivityImages.size()).isEqualTo(activityImages.size());
        for(int i = 0; i < activityImages.size(); i++){
            Assertions.assertThat(activityImages.get(i).getClub().getId()).isEqualTo(club.getId());
            Assertions.assertThat(clubActivityImages.get(i).getId()).isEqualTo(activityImages.get(i).getId());
        }
    }

    @Test
    public void findClubByNameContaining_Default_Success() throws Exception{
        //given
        PageRequest request = PageRequest.of(0, 3, Sort.Direction.ASC, "name");
        clubRepository.save(testDataRepository.getCleanClub(0));
        clubRepository.save(testDataRepository.getCleanClub(1));
        clubRepository.save(testDataRepository.getCleanClub(2));
        clubRepository.save(testDataRepository.getCleanClub(3));
        clubRepository.save(testDataRepository.getCleanClub(4));
        clubRepository.save(testDataRepository.getCleanClub(5));
        em.flush();
        em.clear();

        //when
        Page<Club> firstWordSame = clubRepository.findClubByNameContaining("정", request);
        em.clear();
        Page<Club> middleWordSame = clubRepository.findClubByNameContaining("럽 S", request);
        Page<Club> blank = clubRepository.findClubByNameContaining(" ", request);
        Page<Club> exactlySame = clubRepository.findClubByNameContaining("정상적인 클럽 SKKULOL0", request);

        //then
        Assertions.assertThat(firstWordSame.getTotalElements()).isEqualTo(6);
        Assertions.assertThat(middleWordSame.getTotalElements()).isEqualTo(6);
        Assertions.assertThat(blank.getTotalElements()).isEqualTo(6);
        Assertions.assertThat(exactlySame.getTotalElements()).isEqualTo(1);

    }
    
    @Test
    public void findClubByCampus_Default_FetchLogoAndOrderByNameFirst() throws Exception{
        //given
        Campus campus = Campus.율전;
        Long cnt = em.createQuery("select count(c) from Club c inner join c.logo l where c.campus = :campus", Long.class)
                .setParameter("campus", campus)
                .getSingleResult();
        log.info("cnt : {}", cnt);
        for(int i = 0; i < 8; i++){
            Logo logo = testDataRepository.getLogos().get(i);
            Club club = testDataRepository.getCleanClub(i);
            Optional<Recruit> recruit = Optional.ofNullable(testDataRepository.getRecruits().get(i));
            recruit.ifPresent(club::startRecruit);
            club.changeLogo(logo);
            if(i % 2 == 1){
                Field declaredField = club.getClass().getDeclaredField("campus");
                declaredField.setAccessible(true);
                declaredField.set(club, Campus.율전);
            }
            recruit.ifPresent(recruitRepository::save);
            clubRepository.save(club);
        }
        em.flush();
        em.clear();
        PageRequest request = PageRequest.of(0, 3, Sort.Direction.ASC, "name");

        //when
        Page<Club> clubs = clubRepository.findClubByCampus(campus, request);

        //then
        Assertions.assertThat(clubs.getTotalElements()).isEqualTo(cnt + 4);
        clubs.stream().forEach(c -> Assertions.assertThat(c.getCampus()).isEqualTo(Campus.율전));
        for(int i = 0; i < 2; i++){
            Assertions.assertThat(
                            clubs.getContent().get(i).getName()
                                    .compareTo(clubs.getContent().get(i + 1).getName()))
                    .isLessThan(0);
        }
    }

    @Test
    public void findClubByCampusAndClubType_Default_FetchLogoAndorderByNameFirst() throws Exception{
        //given
        for(int i = 0; i < 8; i++){
            Logo logo = testDataRepository.getLogos().get(i);
            Club club = testDataRepository.getCleanClub(i);
            Optional<Recruit> recruit = Optional.ofNullable(testDataRepository.getRecruits().get(i));
            recruit.ifPresent(club::startRecruit);
            club.changeLogo(logo);
            if(i % 2 == 1){
                Field declaredField = club.getClass().getDeclaredField("clubType");
                declaredField.setAccessible(true);
                declaredField.set(club, ClubType.기타동아리);
            }
            recruit.ifPresent(recruitRepository::save);
            clubRepository.save(club);
        }
        em.flush();
        em.clear();
        PageRequest request = PageRequest.of(0, 3, Sort.Direction.ASC, "name");
        Campus campus = Campus.명륜;
        ClubType clubType = ClubType.기타동아리;

        //when
        Page<Club> clubs = clubRepository.findClubByCampusAndClubType(campus, clubType, request);

        //then
        Assertions.assertThat(clubs.getTotalElements()).isEqualTo(4);
        clubs.stream().forEach(c -> Assertions.assertThat(c.getClubType()).isEqualTo(clubType));
        for(int i = 0; i < 2; i++){
            Assertions.assertThat(clubs.getContent().get(i).getId()).isLessThan(clubs.getContent().get(i + 1).getId());
        }
    }

    @Test
    public void findClubByCampusAndClubTypeAndBelongs_Default_FetchLogoAndorderByNameFirst() throws Exception{
        //given
        for(int i = 0; i < 8; i++){
            Logo logo = testDataRepository.getLogos().get(i);
            Club club = testDataRepository.getCleanClub(i);
            Optional<Recruit> recruit = Optional.ofNullable(testDataRepository.getRecruits().get(i));
            recruit.ifPresent(club::startRecruit);
            club.changeLogo(logo);
            if(i % 2 == 1){
                Field declaredField = club.getClass().getDeclaredField("belongs");
                declaredField.setAccessible(true);
                declaredField.set(club, "종교");
            }
            recruit.ifPresent(recruitRepository::save);
            clubRepository.save(club);
        }
        em.flush();
        em.clear();
        PageRequest request = PageRequest.of(0, 3, Sort.Direction.ASC, "name");
        Campus campus = Campus.명륜;
        ClubType clubType = ClubType.중앙동아리;
        String belongs = "종교";


        //when
        Page<Club> clubs = clubRepository.findClubByCampusAndClubTypeAndBelongs(campus, clubType, belongs, request);

        //then
        Assertions.assertThat(clubs.getTotalElements()).isEqualTo(4);
        clubs.stream().forEach(c -> Assertions.assertThat(c.getBelongs()).isEqualTo(belongs));
        for(int i = 0; i < 2; i++){
            Assertions.assertThat(clubs.getContent().get(i).getId()).isLessThan(clubs.getContent().get(i + 1).getId());
        }
    }

    @Test
    public void findDetailClubById_badClubId_ReturnOptionalEmpty() throws Exception {
        //given
        Long badClubId = -1L;

        //when
        Optional<Club> badIdShouldNull = clubRepository.findDetailClubById(badClubId);

        //then
        Assertions.assertThat(badIdShouldNull).isEmpty();
    }
    
    @Test
    public void random() throws Exception{
        //given
        for(int i = 0; i < 10; i++){
            clubRepository.save(testDataRepository.getCleanClub(i));
        }
        em.flush();
        em.clear();

        //when
        List<Club> clubRandomByCategories1 = clubRepository.findClubRandomByCategories(Campus.명륜.toString());
        em.clear();
        List<Club> clubRandomByCategories2 = clubRepository.findClubRandomByCategories(Campus.명륜.toString());
        em.clear();
        List<Club> clubRandomByCategories3 = clubRepository.findClubRandomByCategories(Campus.명륜.toString());
        em.clear();

        //then
        for(int i = 0; i < 3; i++){
            assertFalse(
                    (clubRandomByCategories1.get(i).equals(clubRandomByCategories2.get(i))
                            && clubRandomByCategories2.get(i).equals(clubRandomByCategories3.get(i)))
                            && clubRandomByCategories3.get(i).equals(clubRandomByCategories1.get(i))
            );
        }
    }
}
