package com.skklub.admin.integration.club;

import com.skklub.admin.TestDataRepository;
import com.skklub.admin.controller.ClubController;
import com.skklub.admin.controller.RecruitController;
import com.skklub.admin.controller.dto.ActivityImageDeletionDTO;
import com.skklub.admin.controller.dto.ClubCreateRequestDTO;
import com.skklub.admin.controller.dto.ClubNameAndIdDTO;
import com.skklub.admin.controller.dto.RecruitDto;
import com.skklub.admin.domain.ActivityImage;
import com.skklub.admin.domain.Club;
import com.skklub.admin.domain.DeletedClub;
import com.skklub.admin.domain.Logo;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@SpringBootTest
@Transactional
@Import(TestDataRepository.class)
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
        ClubCreateRequestDTO clubCreateRequestDTO = testDataRepository.getClubCreateRequestDTO();
        Path path = Paths.get("src/test/resources/img/1.jpg");
        byte[] bytes = Files.readAllBytes(path);
        String logoName = "1.jpg";
        MockMultipartFile logo = new MockMultipartFile("logo", logoName, "image/jpeg", bytes);
        ClubNameAndIdDTO clubNameAndId = clubController.createClub(clubCreateRequestDTO, logo);

        List<MultipartFile> activities = new ArrayList<>();
        int actCnt = 10;
        for(int i = 0; i < actCnt; i++) {
            Path actPath = Paths.get("src/test/resources/img/2.jpg");
            byte[] actBytes = Files.readAllBytes(actPath);
            String actName = "2.jpg";
            activities.add(new MockMultipartFile("activityImage", actName, "image/jpeg", actBytes));
        }
        clubController.uploadActivityImages(clubNameAndId.getId(), activities);

        String recruitQuota = "40~50명";
        String recruitProcessDescription = "test Description";
        RecruitDto recruitDto = RecruitDto.builder()
                .recruitStartAt(LocalDateTime.now())
                .recruitEndAt(LocalDateTime.now())
                .recruitQuota(recruitQuota)
                .recruitProcessDescription(recruitProcessDescription)
                .build();
        recruitController.startRecruit(clubNameAndId.getId(), recruitDto);
        em.flush();
        em.clear();

        //when
        clubController.deleteClubById(clubNameAndId.getId());
        em.flush();
        em.clear();
        
        //then
        Assertions.assertThat(clubRepository.findById(clubNameAndId.getId())).isEmpty();
        Optional<DeletedClub> deletedClub = deletedClubRepository.findById(clubNameAndId.getId());
        Assertions.assertThat(deletedClub).isNotEmpty();
        deletedClub.ifPresent(
                c->{
                    Assertions.assertThat(c.getName()).isEqualTo(clubCreateRequestDTO.getClubName());
                    Assertions.assertThat(c.getActivityDescription()).isEqualTo(clubCreateRequestDTO.getActivityDescription());
                    Assertions.assertThat(c.getBriefActivityDescription()).isEqualTo(clubCreateRequestDTO.getBriefActivityDescription());
                    Assertions.assertThat(c.getClubDescription()).isEqualTo(clubCreateRequestDTO.getClubDescription());
                    Assertions.assertThat(c.getBelongs()).isEqualTo(clubCreateRequestDTO.getBelongs());
                    Assertions.assertThat(c.getCampus()).isEqualTo(clubCreateRequestDTO.getCampus());
                    Assertions.assertThat(c.getClubType()).isEqualTo(clubCreateRequestDTO.getClubType());
                    Assertions.assertThat(c.getEstablishAt()).isEqualTo(clubCreateRequestDTO.getEstablishDate());
                    Assertions.assertThat(c.getHeadLine()).isEqualTo(clubCreateRequestDTO.getHeadLine());
                    Assertions.assertThat(c.getMandatoryActivatePeriod()).isEqualTo(clubCreateRequestDTO.getMandatoryActivatePeriod());
                    Assertions.assertThat(c.getMemberAmount()).isEqualTo(clubCreateRequestDTO.getMemberAmount());
                    Assertions.assertThat(c.getRegularMeetingTime()).isEqualTo(clubCreateRequestDTO.getRegularMeetingTime());
                    Assertions.assertThat(c.getRoomLocation()).isEqualTo(clubCreateRequestDTO.getRoomLocation());
                    Assertions.assertThat(c.getWebLink1()).isEqualTo(clubCreateRequestDTO.getWebLink1());
                    Assertions.assertThat(c.getWebLink2()).isEqualTo(clubCreateRequestDTO.getWebLink2());
                }
        );
        List recruitList = em.createQuery("select r from Club c inner join Recruit r on c.recruit.id = r.id where c.id = :clubId")
                .setParameter("clubId", clubNameAndId.getId())
                .getResultList();
        Assertions.assertThat(recruitList).isEmpty();
        List activityList = em.createQuery("select a from ActivityImage a where a.club.id = :id")
                .setParameter("id", clubNameAndId.getId())
                .getResultList();
        Assertions.assertThat(activityList).isEmpty();
    }

    @Test
    public void reviveClub_AfterDeleteFullRelationClub() throws Exception{
        //given
        ClubCreateRequestDTO clubCreateRequestDTO = testDataRepository.getClubCreateRequestDTO();
        Path path = Paths.get("src/test/resources/img/1.jpg");
        byte[] bytes = Files.readAllBytes(path);
        String logoName = "1.jpg";
        MockMultipartFile logo = new MockMultipartFile("logo", logoName, "image/jpeg", bytes);
        ClubNameAndIdDTO clubNameAndId = clubController.createClub(clubCreateRequestDTO, logo);

        List<MultipartFile> activities = new ArrayList<>();
        int actCnt = 10;
        for(int i = 0; i < actCnt; i++) {
            Path actPath = Paths.get("src/test/resources/img/2.jpg");
            byte[] actBytes = Files.readAllBytes(actPath);
            String actName = "2.jpg";
            activities.add(new MockMultipartFile("activityImage", actName, "image/jpeg", actBytes));
        }
        clubController.uploadActivityImages(clubNameAndId.getId(), activities);

        String recruitQuota = "40~50명";
        String recruitProcessDescription = "test Description";
        RecruitDto recruitDto = RecruitDto.builder()
                .recruitStartAt(LocalDateTime.now())
                .recruitEndAt(LocalDateTime.now())
                .recruitQuota(recruitQuota)
                .recruitProcessDescription(recruitProcessDescription)
                .build();
        recruitController.startRecruit(clubNameAndId.getId(), recruitDto);

        clubController.deleteClubById(clubNameAndId.getId());
        em.flush();
        em.clear();

        //when
        ClubNameAndIdDTO response = clubController.cancelClubDeletionById(clubNameAndId.getId()).getBody();
        em.flush();
        em.clear();

        //then
        Assertions.assertThat(response.getId()).isEqualTo(clubNameAndId.getId());
        Assertions.assertThat(response.getName()).isEqualTo(clubNameAndId.getName());
        Optional<Club> club = clubRepository.findById(response.getId());
        Assertions.assertThat(club).isNotEmpty();
        club.ifPresent(
                c ->{
                    Assertions.assertThat(c.getName()).isEqualTo(clubCreateRequestDTO.getClubName());
                    Assertions.assertThat(c.getActivityDescription()).isEqualTo(clubCreateRequestDTO.getActivityDescription());
                    Assertions.assertThat(c.getBriefActivityDescription()).isEqualTo(clubCreateRequestDTO.getBriefActivityDescription());
                    Assertions.assertThat(c.getClubDescription()).isEqualTo(clubCreateRequestDTO.getClubDescription());
                    Assertions.assertThat(c.getBelongs()).isEqualTo(clubCreateRequestDTO.getBelongs());
                    Assertions.assertThat(c.getCampus()).isEqualTo(clubCreateRequestDTO.getCampus());
                    Assertions.assertThat(c.getClubType()).isEqualTo(clubCreateRequestDTO.getClubType());
                    Assertions.assertThat(c.getEstablishAt()).isEqualTo(clubCreateRequestDTO.getEstablishDate());
                    Assertions.assertThat(c.getHeadLine()).isEqualTo(clubCreateRequestDTO.getHeadLine());
                    Assertions.assertThat(c.getMandatoryActivatePeriod()).isEqualTo(clubCreateRequestDTO.getMandatoryActivatePeriod());
                    Assertions.assertThat(c.getMemberAmount()).isEqualTo(clubCreateRequestDTO.getMemberAmount());
                    Assertions.assertThat(c.getRegularMeetingTime()).isEqualTo(clubCreateRequestDTO.getRegularMeetingTime());
                    Assertions.assertThat(c.getRoomLocation()).isEqualTo(clubCreateRequestDTO.getRoomLocation());
                    Assertions.assertThat(c.getWebLink1()).isEqualTo(clubCreateRequestDTO.getWebLink1());
                    Assertions.assertThat(c.getWebLink2()).isEqualTo(clubCreateRequestDTO.getWebLink2());
                    Assertions.assertThat(c.getRecruit()).isNull();
                    Assertions.assertThat(c.getActivityImages()).isEmpty();
                    Logo cLogo = c.getLogo();
                    Assertions.assertThat(cLogo.getOriginalName()).isEqualTo(logoName);

                }
        );
    }

    @Test
    public void deleteActivityImage() throws Exception{
        //given
        ClubCreateRequestDTO clubCreateRequestDTO = testDataRepository.getClubCreateRequestDTO();
        ClubNameAndIdDTO clubNameAndId = clubController.createClub(clubCreateRequestDTO, null);

        List<MultipartFile> activities = new ArrayList<>();
        int actCnt = 10;
        for(int i = 0; i < actCnt; i++) {
            Path actPath = Paths.get("src/test/resources/img/2.jpg");
            byte[] actBytes = Files.readAllBytes(actPath);
            String actName = i + ".jpg";
            activities.add(new MockMultipartFile("activityImage", actName, "image/jpeg", actBytes));
        }
        clubController.uploadActivityImages(clubNameAndId.getId(), activities);
        em.flush();
        em.clear();

        //when
        ActivityImageDeletionDTO response1 = clubController.deleteActivityImage(clubNameAndId.getId(), "1.jpg").getBody();
        ActivityImageDeletionDTO response2 = clubController.deleteActivityImage(clubNameAndId.getId(), "2.jpg").getBody();
        em.flush();
        em.clear();

        //then
        Assertions.assertThat(response1.getDeletedActivityImageName()).isEqualTo("1.jpg");
        Assertions.assertThat(response2.getDeletedActivityImageName()).isEqualTo("2.jpg");
        Optional<Club> club = clubRepository.findById(clubNameAndId.getId());
        Assertions.assertThat(club).isNotEmpty();
        club.ifPresent(
                c ->{
                    List<ActivityImage> activityImages = c.getActivityImages();
                    Assertions.assertThat(activityImages.size()).isEqualTo(actCnt - 2);
                    Assertions.assertThat(activityImages.stream().map(ActivityImage::getOriginalName).collect(Collectors.toList()))
                            .containsExactly("0.jpg", "3.jpg", "4.jpg", "5.jpg", "6.jpg", "7.jpg", "8.jpg", "9.jpg");
                }
        );

    }

}
