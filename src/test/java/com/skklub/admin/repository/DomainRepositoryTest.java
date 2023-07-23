package com.skklub.admin.repository;

import com.skklub.admin.TestDataRepository;
import com.skklub.admin.domain.*;
import com.skklub.admin.domain.enums.Campus;
import com.skklub.admin.domain.enums.ClubType;
import com.skklub.admin.domain.enums.Role;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

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
    @Autowired
    private DeletedClubRepository deletedClubRepository;

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
        //2 Insert
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
    public void clubRemove_GivenClub_movedFromClubTableToDeletedClubTable() throws Exception{
        //given
        Club club = testDataRepository.getCleanClub(0);
        Logo logo = testDataRepository.getLogos().get(0);
        Recruit recruit = testDataRepository.getRecruits().get(0);
        User user = testDataRepository.getUsers().get(0);
        club.setUser(user);
        em.persist(user);
        club.startRecruit(recruit);
        em.persist(recruit);
        club.changeLogo(logo);
        clubRepository.save(club);
        em.flush();
        em.clear();

        //when
        Optional<Club> findedClub = clubRepository.findById(club.getId());
        findedClub.ifPresent(clubRepository::delete);
        //1 deleted, (1 insert)
        em.flush();
        em.clear();

        //then
        Optional<Club> afterDeletion = clubRepository.findById(club.getId());
        Assertions.assertThat(afterDeletion).isEmpty();
        Optional<DeletedClub> findedDeletedClub = deletedClubRepository.findById(club.getId());
        Assertions.assertThat(findedDeletedClub.get().getId()).isEqualTo(club.getId());
        Assertions.assertThat(findedDeletedClub.get().getCampus()).isEqualTo(club.getCampus());
        Assertions.assertThat(findedDeletedClub.get().getClubType()).isEqualTo(club.getClubType());
        Assertions.assertThat(findedDeletedClub.get().getBelongs()).isEqualTo(club.getBelongs());
        Assertions.assertThat(findedDeletedClub.get().getBriefActivityDescription()).isEqualTo(club.getBriefActivityDescription());
        Assertions.assertThat(findedDeletedClub.get().getName()).isEqualTo(club.getName());
        Assertions.assertThat(findedDeletedClub.get().getHeadLine()).isEqualTo(club.getHeadLine());
        Assertions.assertThat(findedDeletedClub.get().getEstablishAt()).isEqualTo(club.getEstablishAt());
        Assertions.assertThat(findedDeletedClub.get().getRoomLocation()).isEqualTo(club.getRoomLocation());
        Assertions.assertThat(findedDeletedClub.get().getMemberAmount()).isEqualTo(club.getMemberAmount());
        Assertions.assertThat(findedDeletedClub.get().getRegularMeetingTime()).isEqualTo(club.getRegularMeetingTime());
        Assertions.assertThat(findedDeletedClub.get().getMandatoryActivatePeriod()).isEqualTo(club.getMandatoryActivatePeriod());
        Assertions.assertThat(findedDeletedClub.get().getClubDescription()).isEqualTo(club.getClubDescription());
        Assertions.assertThat(findedDeletedClub.get().getActivityDescription()).isEqualTo(club.getActivityDescription());
        Assertions.assertThat(findedDeletedClub.get().getLogoId()).isEqualTo(club.getLogo().getId());
        Assertions.assertThat(findedDeletedClub.get().getWebLink1()).isEqualTo(club.getWebLink1());
        Assertions.assertThat(findedDeletedClub.get().getWebLink2()).isEqualTo(club.getWebLink2());
        Assertions.assertThat(findedDeletedClub.get().getUserId()).isEqualTo(club.getPresident().getId());
        Assertions.assertThat(findedDeletedClub.get().getRecruitId()).isNull();
    }

    @Test
    public void clubRevive_GivenDeletedClub_movedFromDeletedClubTableToClubTable() throws Exception{
        //given
        Club club = testDataRepository.getCleanClub(0);
        Logo logo = testDataRepository.getLogos().get(0);
        Recruit recruit = testDataRepository.getRecruits().get(0);
        User user = testDataRepository.getUsers().get(0);
        club.setUser(user);
        em.persist(user);
        club.startRecruit(recruit);
        em.persist(recruit);
        club.changeLogo(logo);
        clubRepository.save(club);
        em.flush();
        em.clear();
        Optional<Club> findedClub = clubRepository.findById(club.getId());
        findedClub.ifPresent(clubRepository::delete);
        em.flush();
        em.clear();

        //when
        Optional<DeletedClub> findedDeletedClub = deletedClubRepository.findById(club.getId());
        findedDeletedClub.ifPresent(deletedClubRepository::delete);
        //1 update
        em.flush();
        em.clear();

        //then
        Optional<DeletedClub> afterReviveDeletedClub = deletedClubRepository.findById(club.getId());
        Assertions.assertThat(afterReviveDeletedClub).isEmpty();
        Optional<Club> afterReviveClub = clubRepository.findById(club.getId());
        Assertions.assertThat(afterReviveClub).isNotEmpty();
        afterReviveClub.ifPresent(
                c -> {
                    Assertions.assertThat(c.getId()).isEqualTo(club.getId());
                    Assertions.assertThat(c.getCampus()).isEqualTo(club.getCampus());
                    Assertions.assertThat(c.getClubType()).isEqualTo(club.getClubType());
                    Assertions.assertThat(c.getBelongs()).isEqualTo(club.getBelongs());
                    Assertions.assertThat(c.getBriefActivityDescription()).isEqualTo(club.getBriefActivityDescription());
                    Assertions.assertThat(c.getName()).isEqualTo(club.getName());
                    Assertions.assertThat(c.getHeadLine()).isEqualTo(club.getHeadLine());
                    Assertions.assertThat(c.getEstablishAt()).isEqualTo(club.getEstablishAt());
                    Assertions.assertThat(c.getRoomLocation()).isEqualTo(club.getRoomLocation());
                    Assertions.assertThat(c.getMemberAmount()).isEqualTo(club.getMemberAmount());
                    Assertions.assertThat(c.getRegularMeetingTime()).isEqualTo(club.getRegularMeetingTime());
                    Assertions.assertThat(c.getMandatoryActivatePeriod()).isEqualTo(club.getMandatoryActivatePeriod());
                    Assertions.assertThat(c.getClubDescription()).isEqualTo(club.getClubDescription());
                    Assertions.assertThat(c.getActivityDescription()).isEqualTo(club.getActivityDescription());
                    Assertions.assertThat(c.getLogo()).isEqualTo(club.getLogo());
                    Assertions.assertThat(c.getWebLink1()).isEqualTo(club.getWebLink1());
                    Assertions.assertThat(c.getWebLink2()).isEqualTo(club.getWebLink2());
                    Assertions.assertThat(c.getPresident().getContact()).isEqualTo(club.getPresident().getContact());
                    Assertions.assertThat(c.getPresident().getId()).isEqualTo(club.getPresident().getId());
                    Assertions.assertThat(c.getPresident().getName()).isEqualTo(club.getPresident().getName());
                    Assertions.assertThat(c.getPresident().getUsername()).isEqualTo(club.getPresident().getUsername());
                    Assertions.assertThat(c.getRecruit()).isNull();
                }
        );
    }

    @Test
    public void PendingClubToUser_WithHashedPw_SamePw() throws Exception{
        //given
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String encodedPw = bCryptPasswordEncoder.encode("testPw");
        String username = "testUserId";
        String name = "testUser";
        String contact = "testContact";
        PendingClub pendingClub = new PendingClub(
                "testPendingName",
                "testBriefDescription",
                "testActivityDescription",
                "testClubDescription",
                username,
                encodedPw,
                name,
                contact,
                Role.ROLE_ADMIN_SEOUL_CENTRAL
        );

        //when
        User user = pendingClub.toUser();

        //then
        Assertions.assertThat(user.getUsername()).isEqualTo(username);
        Assertions.assertThat(user.getName()).isEqualTo(name);
        Assertions.assertThat(user.getPassword()).isEqualTo(encodedPw);
        Assertions.assertThat(user.getContact()).isEqualTo(contact);
    }

    @Test
    public void PendingClubToClub_GivenUser_NullAtSomeFieldsWithDefaultLogo() throws Exception{
        //given
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String encodedPw = bCryptPasswordEncoder.encode("testPw");
        String username = "testUserId";
        String name = "testUser";
        String contact = "testContact";
        PendingClub pendingClub = new PendingClub(
                "testPendingName",
                "testBriefDescription",
                "testActivityDescription",
                "testClubDescription",
                username,
                encodedPw,
                name,
                contact,
                Role.ROLE_ADMIN_SEOUL_CENTRAL
        );
        User user = pendingClub.toUser();

        //when
        Campus campus = Campus.명륜;
        ClubType clubType = ClubType.중앙동아리;
        String belongs = "종교";
        Club club = pendingClub.toClubWithDefaultLogo(campus, clubType, belongs, user);

        //then
        Logo logo = club.getLogo();
        Assertions.assertThat(logo.getOriginalName()).isEqualTo("alt.jpg");
        Assertions.assertThat(logo.getUploadedName()).isEqualTo("alt.jpg");
        Assertions.assertThat(club.getName()).isEqualTo(pendingClub.getClubName());
        Assertions.assertThat(club.getBriefActivityDescription()).isEqualTo(pendingClub.getBriefActivityDescription());
        Assertions.assertThat(club.getActivityDescription()).isEqualTo(pendingClub.getActivityDescription());
        Assertions.assertThat(club.getClubDescription()).isEqualTo(pendingClub.getClubDescription());
        Assertions.assertThat(club.getCampus()).isEqualTo(campus);
        Assertions.assertThat(club.getClubType()).isEqualTo(clubType);
        Assertions.assertThat(club.getBelongs()).isEqualTo(belongs);
        Assertions.assertThat(club.getHeadLine()).isNull();
        Assertions.assertThat(club.getEstablishAt()).isNull();
        Assertions.assertThat(club.getRoomLocation()).isNull();
        Assertions.assertThat(club.getMemberAmount()).isNull();
        Assertions.assertThat(club.getRegularMeetingTime()).isNull();
        Assertions.assertThat(club.getMandatoryActivatePeriod()).isNull();
        Assertions.assertThat(club.getWebLink1()).isNull();
        Assertions.assertThat(club.getWebLink2()).isNull();
        Assertions.assertThat(club.getRecruit()).isNull();
        Assertions.assertThat(club.getActivityImages()).isEmpty();
        Assertions.assertThat(club.getPresident()).isEqualTo(user);
    }
}
