package com.skklub.admin.integration.club;

import com.skklub.admin.TestDataRepository;
import com.skklub.admin.WithMockCustomUser;
import com.skklub.admin.controller.ClubController;
import com.skklub.admin.controller.RecruitController;
import com.skklub.admin.controller.dto.ActivityImageDeletionDTO;
import com.skklub.admin.controller.dto.ClubNameAndIdDTO;
import com.skklub.admin.domain.imagefile.ActivityImage;
import com.skklub.admin.domain.Club;
import com.skklub.admin.domain.DeletedClub;
import com.skklub.admin.domain.imagefile.Logo;
import com.skklub.admin.domain.enums.Role;
import com.skklub.admin.repository.ClubRepository;
import com.skklub.admin.repository.DeletedClubRepository;
import com.skklub.admin.repository.RecruitRepository;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@SpringBootTest
@Transactional
@Import(TestDataRepository.class)
@WithMockCustomUser(username = "testMasterID",role = Role.ROLE_MASTER)
public class ClubDeleteIntegrationTest {
    @Autowired
    private TestDataRepository testDataRepository;
    @Autowired
    private EntityManager em;
    @Autowired
    private ClubController clubController;
    @Autowired
    private ClubRepository clubRepository;
    @Autowired
    private DeletedClubRepository deletedClubRepository;
    @Autowired
    private RecruitController recruitController;
    @Autowired
    private RecruitRepository recruitRepository;

    @Test
    public void deleteClub_GivenClubWithFullRelation() throws Exception{
        //given
        Club club = em.createQuery("select c from Club c inner join fetch c.logo inner join fetch c.activityImages inner join fetch c.recruit", Club.class)
                .setMaxResults(1)
                .getSingleResult();
        em.clear();

        //when
        clubController.deleteClubById(club.getId());
        em.flush();
        em.clear();
        
        //then
        Assertions.assertThat(clubRepository.findById(club.getId())).isEmpty();
        Optional<DeletedClub> deletedClub = deletedClubRepository.findById(club.getId());
        Assertions.assertThat(deletedClub).isNotEmpty();
        deletedClub.ifPresent(
                c->{
                    Assertions.assertThat(c.getName()).isEqualTo(club.getName());
                    Assertions.assertThat(c.getActivityDescription()).isEqualTo(club.getActivityDescription());
                    Assertions.assertThat(c.getBriefActivityDescription()).isEqualTo(club.getBriefActivityDescription());
                    Assertions.assertThat(c.getClubDescription()).isEqualTo(club.getClubDescription());
                    Assertions.assertThat(c.getBelongs()).isEqualTo(club.getBelongs());
                    Assertions.assertThat(c.getCampus()).isEqualTo(club.getCampus());
                    Assertions.assertThat(c.getClubType()).isEqualTo(club.getClubType());
                    Assertions.assertThat(c.getEstablishAt()).isEqualTo(club.getEstablishAt());
                    Assertions.assertThat(c.getHeadLine()).isEqualTo(club.getHeadLine());
                    Assertions.assertThat(c.getMandatoryActivatePeriod()).isEqualTo(club.getMandatoryActivatePeriod());
                    Assertions.assertThat(c.getMemberAmount()).isEqualTo(club.getMemberAmount());
                    Assertions.assertThat(c.getRegularMeetingTime()).isEqualTo(club.getRegularMeetingTime());
                    Assertions.assertThat(c.getRoomLocation()).isEqualTo(club.getRoomLocation());
                    Assertions.assertThat(c.getWebLink1()).isEqualTo(club.getWebLink1());
                    Assertions.assertThat(c.getWebLink2()).isEqualTo(club.getWebLink2());
                }
        );
        List recruitList = em.createQuery("select r from Club c inner join Recruit r on c.recruit.id = r.id where c.id = :clubId")
                .setParameter("clubId", club.getId())
                .getResultList();
        Assertions.assertThat(recruitList).isEmpty();
        List activityList = em.createQuery("select a from ActivityImage a where a.club.id = :id")
                .setParameter("id", club.getId())
                .getResultList();
        Assertions.assertThat(activityList).isEmpty();
    }

    @Test
    public void reviveClub_AfterDeleteFullRelationClub() throws Exception{
        //given
        Club club = em.createQuery("select c from Club c inner join fetch c.logo inner join fetch c.activityImages inner join fetch c.recruit inner join fetch c.president inner join fetch c.president", Club.class)
                .setMaxResults(1)
                .getSingleResult();
        Logo logoBefore = club.getLogo();
        ClubNameAndIdDTO clubNameAndId = clubController.deleteClubById(club.getId()).getBody();
        em.flush();
        em.clear();

        //when
        ClubNameAndIdDTO response = clubController.cancelClubDeletionById(clubNameAndId.getId()).getBody();
        em.flush();
        em.clear();

        //then
        Assertions.assertThat(response.getId()).isEqualTo(clubNameAndId.getId());
        Assertions.assertThat(response.getName()).isEqualTo(clubNameAndId.getName());
        Optional<Club> reviveClub = clubRepository.findById(response.getId());
        Assertions.assertThat(reviveClub).isNotEmpty();
        reviveClub.ifPresent(
                c ->{
                    Assertions.assertThat(c.getName()).isEqualTo(club.getName());
                    Assertions.assertThat(c.getActivityDescription()).isEqualTo(club.getActivityDescription());
                    Assertions.assertThat(c.getBriefActivityDescription()).isEqualTo(club.getBriefActivityDescription());
                    Assertions.assertThat(c.getClubDescription()).isEqualTo(club.getClubDescription());
                    Assertions.assertThat(c.getBelongs()).isEqualTo(club.getBelongs());
                    Assertions.assertThat(c.getCampus()).isEqualTo(club.getCampus());
                    Assertions.assertThat(c.getClubType()).isEqualTo(club.getClubType());
                    Assertions.assertThat(c.getEstablishAt()).isEqualTo(club.getEstablishAt());
                    Assertions.assertThat(c.getHeadLine()).isEqualTo(club.getHeadLine());
                    Assertions.assertThat(c.getMandatoryActivatePeriod()).isEqualTo(club.getMandatoryActivatePeriod());
                    Assertions.assertThat(c.getMemberAmount()).isEqualTo(club.getMemberAmount());
                    Assertions.assertThat(c.getRegularMeetingTime()).isEqualTo(club.getRegularMeetingTime());
                    Assertions.assertThat(c.getRoomLocation()).isEqualTo(club.getRoomLocation());
                    Assertions.assertThat(c.getWebLink1()).isEqualTo(club.getWebLink1());
                    Assertions.assertThat(c.getWebLink2()).isEqualTo(club.getWebLink2());
                    Assertions.assertThat(c.getRecruit()).isNull();
                    Assertions.assertThat(c.getActivityImages()).isEmpty();
                    Logo logo = c.getLogo();
                    Assertions.assertThat(logo.getOriginalName()).isEqualTo(logoBefore.getOriginalName());
                    Assertions.assertThat(logo.getUploadedName()).isEqualTo(logoBefore.getUploadedName());

                }
        );
    }

    @Test
    public void deleteActivityImage() throws Exception{
        //given
        List<Club> clubs = em.createQuery("select c from Club c inner join fetch c.activityImages", Club.class)
                .getResultList();
        Club club = clubs.stream().filter(c -> c.getActivityImages().size() > 1).findFirst().get();
        List<ActivityImage> activityImages = club.getActivityImages();
        em.clear();

        //when
        ActivityImageDeletionDTO response1 = clubController.deleteActivityImage(club.getId(), activityImages.get(0).getOriginalName()).getBody();
        ActivityImageDeletionDTO response2 = clubController.deleteActivityImage(club.getId(), activityImages.get(1).getOriginalName()).getBody();
        em.flush();
        em.clear();

        //then
        Assertions.assertThat(response1.getDeletedActivityImageName()).isEqualTo(activityImages.get(0).getOriginalName());
        Assertions.assertThat(response2.getDeletedActivityImageName()).isEqualTo(activityImages.get(1).getOriginalName());
        Optional<Club> clubAfterImgDeletion = clubRepository.findById(club.getId());
        Assertions.assertThat(clubAfterImgDeletion).isNotEmpty();
        clubAfterImgDeletion.ifPresent(
                c ->{
                    List<ActivityImage> actImgs = c.getActivityImages();
                    Assertions.assertThat(actImgs.size()).isEqualTo(activityImages.size() - 2);
                    Assertions.assertThat(actImgs.stream().map(ActivityImage::getOriginalName).collect(Collectors.toList()))
                            .doesNotContain(activityImages.get(0).getOriginalName(), activityImages.get(1).getOriginalName());
                }
        );

    }

}
