package com.skklub.admin.integration.club;

import com.skklub.admin.InitDatabase;
import com.skklub.admin.TestDataRepository;
import com.skklub.admin.controller.ClubController;
import com.skklub.admin.controller.dto.*;
import com.skklub.admin.domain.enums.Campus;
import com.skklub.admin.domain.enums.ClubType;
import com.skklub.admin.repository.ClubRepository;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;

@Slf4j
@Transactional
@SpringBootTest
@Import({TestDataRepository.class, InitDatabase.class})
public class ClubReadIntegrationTest {
    @Autowired
    private EntityManager em;
    @Autowired
    private ClubController clubController;
    @Autowired
    private ClubRepository clubRepository;
    @Autowired
    private TestDataRepository testDataRepository;

    /**
     * select club_id, logo.original_name, count(activity_image_id), recruit_id, (start_at is null)
     * from club
     * left join logo using(logo_id)
     * left join activity_image using(club_id)
     * left join recruit using(recruit_id)
     * group by club_id
     */
    @Test
    public void getClubById_DefaultLogo() throws Exception {
        //given

        //when
        ClubResponseDTO response = clubController.getClubById(1L).getBody();

        //then
        ClubCreateRequestDTO clubCreateRequestDTO = testDataRepository.getClubCreateRequestDTO(0);
        Assertions.assertThat(response.getName()).isEqualTo(clubCreateRequestDTO.getClubName());
        Assertions.assertThat(response.getActivityDescription()).isEqualTo(clubCreateRequestDTO.getActivityDescription());
        Assertions.assertThat(response.getBriefActivityDescription()).isEqualTo(clubCreateRequestDTO.getBriefActivityDescription());
        Assertions.assertThat(response.getClubDescription()).isEqualTo(clubCreateRequestDTO.getClubDescription());
        Assertions.assertThat(response.getBelongs()).isEqualTo(clubCreateRequestDTO.getBelongs());
        Assertions.assertThat(response.getCampus()).isEqualTo(clubCreateRequestDTO.getCampus());
        Assertions.assertThat(response.getClubType()).isEqualTo(clubCreateRequestDTO.getClubType());
        Assertions.assertThat(response.getEstablishAt()).isEqualTo(clubCreateRequestDTO.getEstablishDate());
        Assertions.assertThat(response.getHeadLine()).isEqualTo(clubCreateRequestDTO.getHeadLine());
        Assertions.assertThat(response.getMandatoryActivatePeriod()).isEqualTo(clubCreateRequestDTO.getMandatoryActivatePeriod());
        Assertions.assertThat(response.getMemberAmount()).isEqualTo(clubCreateRequestDTO.getMemberAmount());
        Assertions.assertThat(response.getRegularMeetingTime()).isEqualTo(clubCreateRequestDTO.getRegularMeetingTime());
        Assertions.assertThat(response.getRoomLocation()).isEqualTo(clubCreateRequestDTO.getRoomLocation());
        Assertions.assertThat(response.getWebLink1()).isEqualTo(clubCreateRequestDTO.getWebLink1());
        Assertions.assertThat(response.getWebLink2()).isEqualTo(clubCreateRequestDTO.getWebLink2());
        Assertions.assertThat(response.getLogo().getFileName()).isEqualTo("alt.jpg");
        Assertions.assertThat(response.getLogo().getBytes()).isNotNull();
        Assertions.assertThat(response.getLogo().getId()).isNotNull();
    }

    @Test
    public void getClubById_SomeLogo() throws Exception {
        //given

        //when
        ClubResponseDTO response = clubController.getClubById(2L).getBody();


        //then
        ClubCreateRequestDTO clubCreateRequestDTO = testDataRepository.getClubCreateRequestDTO(1);
        Assertions.assertThat(response.getName()).isEqualTo(clubCreateRequestDTO.getClubName());
        Assertions.assertThat(response.getActivityDescription()).isEqualTo(clubCreateRequestDTO.getActivityDescription());
        Assertions.assertThat(response.getBriefActivityDescription()).isEqualTo(clubCreateRequestDTO.getBriefActivityDescription());
        Assertions.assertThat(response.getClubDescription()).isEqualTo(clubCreateRequestDTO.getClubDescription());
        Assertions.assertThat(response.getBelongs()).isEqualTo(clubCreateRequestDTO.getBelongs());
        Assertions.assertThat(response.getCampus()).isEqualTo(clubCreateRequestDTO.getCampus());
        Assertions.assertThat(response.getClubType()).isEqualTo(clubCreateRequestDTO.getClubType());
        Assertions.assertThat(response.getEstablishAt()).isEqualTo(clubCreateRequestDTO.getEstablishDate());
        Assertions.assertThat(response.getHeadLine()).isEqualTo(clubCreateRequestDTO.getHeadLine());
        Assertions.assertThat(response.getMandatoryActivatePeriod()).isEqualTo(clubCreateRequestDTO.getMandatoryActivatePeriod());
        Assertions.assertThat(response.getMemberAmount()).isEqualTo(clubCreateRequestDTO.getMemberAmount());
        Assertions.assertThat(response.getRegularMeetingTime()).isEqualTo(clubCreateRequestDTO.getRegularMeetingTime());
        Assertions.assertThat(response.getRoomLocation()).isEqualTo(clubCreateRequestDTO.getRoomLocation());
        Assertions.assertThat(response.getWebLink1()).isEqualTo(clubCreateRequestDTO.getWebLink1());
        Assertions.assertThat(response.getWebLink2()).isEqualTo(clubCreateRequestDTO.getWebLink2());
        Assertions.assertThat(response.getLogo().getFileName()).isNotEqualTo("alt.jpg");
        Assertions.assertThat(response.getLogo().getBytes()).isNotNull();
        Assertions.assertThat(response.getLogo().getId()).isNotNull();
    }

    @Test
    public void getClubById_WithRecruit() throws Exception {
        //given
        int recruitIndex = 2;

        //when
        ClubResponseDTO response = clubController.getClubById(3L).getBody();

        //then
        ClubCreateRequestDTO clubCreateRequestDTO = testDataRepository.getClubCreateRequestDTO(2);
        Assertions.assertThat(response.getName()).isEqualTo(clubCreateRequestDTO.getClubName());
        Assertions.assertThat(response.getActivityDescription()).isEqualTo(clubCreateRequestDTO.getActivityDescription());
        Assertions.assertThat(response.getBriefActivityDescription()).isEqualTo(clubCreateRequestDTO.getBriefActivityDescription());
        Assertions.assertThat(response.getClubDescription()).isEqualTo(clubCreateRequestDTO.getClubDescription());
        Assertions.assertThat(response.getBelongs()).isEqualTo(clubCreateRequestDTO.getBelongs());
        Assertions.assertThat(response.getCampus()).isEqualTo(clubCreateRequestDTO.getCampus());
        Assertions.assertThat(response.getClubType()).isEqualTo(clubCreateRequestDTO.getClubType());
        Assertions.assertThat(response.getEstablishAt()).isEqualTo(clubCreateRequestDTO.getEstablishDate());
        Assertions.assertThat(response.getHeadLine()).isEqualTo(clubCreateRequestDTO.getHeadLine());
        Assertions.assertThat(response.getMandatoryActivatePeriod()).isEqualTo(clubCreateRequestDTO.getMandatoryActivatePeriod());
        Assertions.assertThat(response.getMemberAmount()).isEqualTo(clubCreateRequestDTO.getMemberAmount());
        Assertions.assertThat(response.getRegularMeetingTime()).isEqualTo(clubCreateRequestDTO.getRegularMeetingTime());
        Assertions.assertThat(response.getRoomLocation()).isEqualTo(clubCreateRequestDTO.getRoomLocation());
        Assertions.assertThat(response.getWebLink1()).isEqualTo(clubCreateRequestDTO.getWebLink1());
        Assertions.assertThat(response.getWebLink2()).isEqualTo(clubCreateRequestDTO.getWebLink2());

        Assertions.assertThat(response.getRecruit()).isNotEmpty();
        response.getRecruit().ifPresent(
                recruit -> {
                    Assertions.assertThat(recruit.getRecruitStartAt()).isNotNull();
                    Assertions.assertThat(recruit.getRecruitEndAt()).isNotNull();
                    Assertions.assertThat(recruit.getRecruitQuota()).contains(String.valueOf(recruitIndex));
                    Assertions.assertThat(recruit.getRecruitProcessDescription()).contains(String.valueOf(recruitIndex));
                    Assertions.assertThat(recruit.getRecruitContact()).contains(String.valueOf(recruitIndex));
                    Assertions.assertThat(recruit.getRecruitWebLink()).contains(String.valueOf(recruitIndex));

                }
        );
    }

    @Test
    public void getClubById_AlwaysRecruit() throws Exception {
        //given
        int recruitIndex = 1;

        //when
        ClubResponseDTO response = clubController.getClubById(2L).getBody();

        //then
        ClubCreateRequestDTO clubCreateRequestDTO = testDataRepository.getClubCreateRequestDTO(1);
        Assertions.assertThat(response.getName()).isEqualTo(clubCreateRequestDTO.getClubName());
        Assertions.assertThat(response.getActivityDescription()).isEqualTo(clubCreateRequestDTO.getActivityDescription());
        Assertions.assertThat(response.getBriefActivityDescription()).isEqualTo(clubCreateRequestDTO.getBriefActivityDescription());
        Assertions.assertThat(response.getClubDescription()).isEqualTo(clubCreateRequestDTO.getClubDescription());
        Assertions.assertThat(response.getBelongs()).isEqualTo(clubCreateRequestDTO.getBelongs());
        Assertions.assertThat(response.getCampus()).isEqualTo(clubCreateRequestDTO.getCampus());
        Assertions.assertThat(response.getClubType()).isEqualTo(clubCreateRequestDTO.getClubType());
        Assertions.assertThat(response.getEstablishAt()).isEqualTo(clubCreateRequestDTO.getEstablishDate());
        Assertions.assertThat(response.getHeadLine()).isEqualTo(clubCreateRequestDTO.getHeadLine());
        Assertions.assertThat(response.getMandatoryActivatePeriod()).isEqualTo(clubCreateRequestDTO.getMandatoryActivatePeriod());
        Assertions.assertThat(response.getMemberAmount()).isEqualTo(clubCreateRequestDTO.getMemberAmount());
        Assertions.assertThat(response.getRegularMeetingTime()).isEqualTo(clubCreateRequestDTO.getRegularMeetingTime());
        Assertions.assertThat(response.getRoomLocation()).isEqualTo(clubCreateRequestDTO.getRoomLocation());
        Assertions.assertThat(response.getWebLink1()).isEqualTo(clubCreateRequestDTO.getWebLink1());
        Assertions.assertThat(response.getWebLink2()).isEqualTo(clubCreateRequestDTO.getWebLink2());

        Assertions.assertThat(response.getRecruit()).isNotEmpty();
        response.getRecruit().ifPresent(
                recruit -> {
                    Assertions.assertThat(recruit.getRecruitStartAt()).isNull();
                    Assertions.assertThat(recruit.getRecruitEndAt()).isNull();
                    Assertions.assertThat(recruit.getRecruitQuota()).contains(String.valueOf(recruitIndex));
                    Assertions.assertThat(recruit.getRecruitProcessDescription()).contains(String.valueOf(recruitIndex));
                    Assertions.assertThat(recruit.getRecruitContact()).contains(String.valueOf(recruitIndex));
                    Assertions.assertThat(recruit.getRecruitWebLink()).contains(String.valueOf(recruitIndex));

                }
        );
    }

    @Test
    public void getClubById_NoRecruit() throws Exception {
        //given

        //when
        ClubResponseDTO response = clubController.getClubById(1L).getBody();

        //then
        ClubCreateRequestDTO clubCreateRequestDTO = testDataRepository.getClubCreateRequestDTO(0);
        Assertions.assertThat(response.getName()).isEqualTo(clubCreateRequestDTO.getClubName());
        Assertions.assertThat(response.getActivityDescription()).isEqualTo(clubCreateRequestDTO.getActivityDescription());
        Assertions.assertThat(response.getBriefActivityDescription()).isEqualTo(clubCreateRequestDTO.getBriefActivityDescription());
        Assertions.assertThat(response.getClubDescription()).isEqualTo(clubCreateRequestDTO.getClubDescription());
        Assertions.assertThat(response.getBelongs()).isEqualTo(clubCreateRequestDTO.getBelongs());
        Assertions.assertThat(response.getCampus()).isEqualTo(clubCreateRequestDTO.getCampus());
        Assertions.assertThat(response.getClubType()).isEqualTo(clubCreateRequestDTO.getClubType());
        Assertions.assertThat(response.getEstablishAt()).isEqualTo(clubCreateRequestDTO.getEstablishDate());
        Assertions.assertThat(response.getHeadLine()).isEqualTo(clubCreateRequestDTO.getHeadLine());
        Assertions.assertThat(response.getMandatoryActivatePeriod()).isEqualTo(clubCreateRequestDTO.getMandatoryActivatePeriod());
        Assertions.assertThat(response.getMemberAmount()).isEqualTo(clubCreateRequestDTO.getMemberAmount());
        Assertions.assertThat(response.getRegularMeetingTime()).isEqualTo(clubCreateRequestDTO.getRegularMeetingTime());
        Assertions.assertThat(response.getRoomLocation()).isEqualTo(clubCreateRequestDTO.getRoomLocation());
        Assertions.assertThat(response.getWebLink1()).isEqualTo(clubCreateRequestDTO.getWebLink1());
        Assertions.assertThat(response.getWebLink2()).isEqualTo(clubCreateRequestDTO.getWebLink2());

        Assertions.assertThat(response.getRecruit()).isEmpty();
    }

    @Test
    public void getClubById_ManyActImg() throws Exception {
        //given

        //when
        ClubResponseDTO response = clubController.getClubById(6L).getBody();

        //then
        ClubCreateRequestDTO clubCreateRequestDTO = testDataRepository.getClubCreateRequestDTO(5);
        Assertions.assertThat(response.getName()).isEqualTo(clubCreateRequestDTO.getClubName());
        Assertions.assertThat(response.getActivityDescription()).isEqualTo(clubCreateRequestDTO.getActivityDescription());
        Assertions.assertThat(response.getBriefActivityDescription()).isEqualTo(clubCreateRequestDTO.getBriefActivityDescription());
        Assertions.assertThat(response.getClubDescription()).isEqualTo(clubCreateRequestDTO.getClubDescription());
        Assertions.assertThat(response.getBelongs()).isEqualTo(clubCreateRequestDTO.getBelongs());
        Assertions.assertThat(response.getCampus()).isEqualTo(clubCreateRequestDTO.getCampus());
        Assertions.assertThat(response.getClubType()).isEqualTo(clubCreateRequestDTO.getClubType());
        Assertions.assertThat(response.getEstablishAt()).isEqualTo(clubCreateRequestDTO.getEstablishDate());
        Assertions.assertThat(response.getHeadLine()).isEqualTo(clubCreateRequestDTO.getHeadLine());
        Assertions.assertThat(response.getMandatoryActivatePeriod()).isEqualTo(clubCreateRequestDTO.getMandatoryActivatePeriod());
        Assertions.assertThat(response.getMemberAmount()).isEqualTo(clubCreateRequestDTO.getMemberAmount());
        Assertions.assertThat(response.getRegularMeetingTime()).isEqualTo(clubCreateRequestDTO.getRegularMeetingTime());
        Assertions.assertThat(response.getRoomLocation()).isEqualTo(clubCreateRequestDTO.getRoomLocation());
        Assertions.assertThat(response.getWebLink1()).isEqualTo(clubCreateRequestDTO.getWebLink1());
        Assertions.assertThat(response.getWebLink2()).isEqualTo(clubCreateRequestDTO.getWebLink2());

        List<S3DownloadDto> activityImages = response.getActivityImages();
        Assertions.assertThat(activityImages).hasSize(5);
        activityImages.stream()
                .forEach(
                        s3DownloadDto -> {
                            Assertions.assertThat(s3DownloadDto.getId()).isNotNull();
                            Assertions.assertThat(s3DownloadDto.getBytes()).isNotNull();
                            Assertions.assertThat(s3DownloadDto.getFileName()).isEqualTo("4.jpg");
                        }
                );
    }

    @Test
    public void getClubById_OneActImg() throws Exception {
        //given

        //when
        ClubResponseDTO response = clubController.getClubById(2L).getBody();

        //then
        ClubCreateRequestDTO clubCreateRequestDTO = testDataRepository.getClubCreateRequestDTO(1);
        Assertions.assertThat(response.getName()).isEqualTo(clubCreateRequestDTO.getClubName());
        Assertions.assertThat(response.getActivityDescription()).isEqualTo(clubCreateRequestDTO.getActivityDescription());
        Assertions.assertThat(response.getBriefActivityDescription()).isEqualTo(clubCreateRequestDTO.getBriefActivityDescription());
        Assertions.assertThat(response.getClubDescription()).isEqualTo(clubCreateRequestDTO.getClubDescription());
        Assertions.assertThat(response.getBelongs()).isEqualTo(clubCreateRequestDTO.getBelongs());
        Assertions.assertThat(response.getCampus()).isEqualTo(clubCreateRequestDTO.getCampus());
        Assertions.assertThat(response.getClubType()).isEqualTo(clubCreateRequestDTO.getClubType());
        Assertions.assertThat(response.getEstablishAt()).isEqualTo(clubCreateRequestDTO.getEstablishDate());
        Assertions.assertThat(response.getHeadLine()).isEqualTo(clubCreateRequestDTO.getHeadLine());
        Assertions.assertThat(response.getMandatoryActivatePeriod()).isEqualTo(clubCreateRequestDTO.getMandatoryActivatePeriod());
        Assertions.assertThat(response.getMemberAmount()).isEqualTo(clubCreateRequestDTO.getMemberAmount());
        Assertions.assertThat(response.getRegularMeetingTime()).isEqualTo(clubCreateRequestDTO.getRegularMeetingTime());
        Assertions.assertThat(response.getRoomLocation()).isEqualTo(clubCreateRequestDTO.getRoomLocation());
        Assertions.assertThat(response.getWebLink1()).isEqualTo(clubCreateRequestDTO.getWebLink1());
        Assertions.assertThat(response.getWebLink2()).isEqualTo(clubCreateRequestDTO.getWebLink2());

        List<S3DownloadDto> activityImages = response.getActivityImages();
        Assertions.assertThat(activityImages).hasSize(1);
        activityImages.stream()
                .forEach(
                        s3DownloadDto -> {
                            Assertions.assertThat(s3DownloadDto.getId()).isNotNull();
                            Assertions.assertThat(s3DownloadDto.getBytes()).isNotNull();
                            Assertions.assertThat(s3DownloadDto.getFileName()).isEqualTo("4.jpg");
                        }
                );
    }

    @Test
    public void getClubById_NoActImg() throws Exception {
        //given

        //when
        ClubResponseDTO response = clubController.getClubById(1L).getBody();

        //then
        ClubCreateRequestDTO clubCreateRequestDTO = testDataRepository.getClubCreateRequestDTO(0);
        Assertions.assertThat(response.getName()).isEqualTo(clubCreateRequestDTO.getClubName());
        Assertions.assertThat(response.getActivityDescription()).isEqualTo(clubCreateRequestDTO.getActivityDescription());
        Assertions.assertThat(response.getBriefActivityDescription()).isEqualTo(clubCreateRequestDTO.getBriefActivityDescription());
        Assertions.assertThat(response.getClubDescription()).isEqualTo(clubCreateRequestDTO.getClubDescription());
        Assertions.assertThat(response.getBelongs()).isEqualTo(clubCreateRequestDTO.getBelongs());
        Assertions.assertThat(response.getCampus()).isEqualTo(clubCreateRequestDTO.getCampus());
        Assertions.assertThat(response.getClubType()).isEqualTo(clubCreateRequestDTO.getClubType());
        Assertions.assertThat(response.getEstablishAt()).isEqualTo(clubCreateRequestDTO.getEstablishDate());
        Assertions.assertThat(response.getHeadLine()).isEqualTo(clubCreateRequestDTO.getHeadLine());
        Assertions.assertThat(response.getMandatoryActivatePeriod()).isEqualTo(clubCreateRequestDTO.getMandatoryActivatePeriod());
        Assertions.assertThat(response.getMemberAmount()).isEqualTo(clubCreateRequestDTO.getMemberAmount());
        Assertions.assertThat(response.getRegularMeetingTime()).isEqualTo(clubCreateRequestDTO.getRegularMeetingTime());
        Assertions.assertThat(response.getRoomLocation()).isEqualTo(clubCreateRequestDTO.getRoomLocation());
        Assertions.assertThat(response.getWebLink1()).isEqualTo(clubCreateRequestDTO.getWebLink1());
        Assertions.assertThat(response.getWebLink2()).isEqualTo(clubCreateRequestDTO.getWebLink2());

        List<S3DownloadDto> activityImages = response.getActivityImages();
        Assertions.assertThat(activityImages).isEmpty();
    }

    @Test
    public void getClubByName_DefaultLogo() throws Exception {
        //given

        //when
        ClubResponseDTO response = clubController.getClubByName("testClubName0").getBody();

        //then
        ClubCreateRequestDTO clubCreateRequestDTO = testDataRepository.getClubCreateRequestDTO(0);
        Assertions.assertThat(response.getName()).isEqualTo(clubCreateRequestDTO.getClubName());
        Assertions.assertThat(response.getActivityDescription()).isEqualTo(clubCreateRequestDTO.getActivityDescription());
        Assertions.assertThat(response.getBriefActivityDescription()).isEqualTo(clubCreateRequestDTO.getBriefActivityDescription());
        Assertions.assertThat(response.getClubDescription()).isEqualTo(clubCreateRequestDTO.getClubDescription());
        Assertions.assertThat(response.getBelongs()).isEqualTo(clubCreateRequestDTO.getBelongs());
        Assertions.assertThat(response.getCampus()).isEqualTo(clubCreateRequestDTO.getCampus());
        Assertions.assertThat(response.getClubType()).isEqualTo(clubCreateRequestDTO.getClubType());
        Assertions.assertThat(response.getEstablishAt()).isEqualTo(clubCreateRequestDTO.getEstablishDate());
        Assertions.assertThat(response.getHeadLine()).isEqualTo(clubCreateRequestDTO.getHeadLine());
        Assertions.assertThat(response.getMandatoryActivatePeriod()).isEqualTo(clubCreateRequestDTO.getMandatoryActivatePeriod());
        Assertions.assertThat(response.getMemberAmount()).isEqualTo(clubCreateRequestDTO.getMemberAmount());
        Assertions.assertThat(response.getRegularMeetingTime()).isEqualTo(clubCreateRequestDTO.getRegularMeetingTime());
        Assertions.assertThat(response.getRoomLocation()).isEqualTo(clubCreateRequestDTO.getRoomLocation());
        Assertions.assertThat(response.getWebLink1()).isEqualTo(clubCreateRequestDTO.getWebLink1());
        Assertions.assertThat(response.getWebLink2()).isEqualTo(clubCreateRequestDTO.getWebLink2());
        Assertions.assertThat(response.getLogo().getFileName()).isEqualTo("alt.jpg");
        Assertions.assertThat(response.getLogo().getBytes()).isNotNull();
        Assertions.assertThat(response.getLogo().getId()).isNotNull();
    }

    @Test
    public void getClubByName_SomeLogo() throws Exception {
        //given

        //when
        ClubResponseDTO response = clubController.getClubByName("testClubName1").getBody();


        //then
        ClubCreateRequestDTO clubCreateRequestDTO = testDataRepository.getClubCreateRequestDTO(1);
        Assertions.assertThat(response.getName()).isEqualTo(clubCreateRequestDTO.getClubName());
        Assertions.assertThat(response.getActivityDescription()).isEqualTo(clubCreateRequestDTO.getActivityDescription());
        Assertions.assertThat(response.getBriefActivityDescription()).isEqualTo(clubCreateRequestDTO.getBriefActivityDescription());
        Assertions.assertThat(response.getClubDescription()).isEqualTo(clubCreateRequestDTO.getClubDescription());
        Assertions.assertThat(response.getBelongs()).isEqualTo(clubCreateRequestDTO.getBelongs());
        Assertions.assertThat(response.getCampus()).isEqualTo(clubCreateRequestDTO.getCampus());
        Assertions.assertThat(response.getClubType()).isEqualTo(clubCreateRequestDTO.getClubType());
        Assertions.assertThat(response.getEstablishAt()).isEqualTo(clubCreateRequestDTO.getEstablishDate());
        Assertions.assertThat(response.getHeadLine()).isEqualTo(clubCreateRequestDTO.getHeadLine());
        Assertions.assertThat(response.getMandatoryActivatePeriod()).isEqualTo(clubCreateRequestDTO.getMandatoryActivatePeriod());
        Assertions.assertThat(response.getMemberAmount()).isEqualTo(clubCreateRequestDTO.getMemberAmount());
        Assertions.assertThat(response.getRegularMeetingTime()).isEqualTo(clubCreateRequestDTO.getRegularMeetingTime());
        Assertions.assertThat(response.getRoomLocation()).isEqualTo(clubCreateRequestDTO.getRoomLocation());
        Assertions.assertThat(response.getWebLink1()).isEqualTo(clubCreateRequestDTO.getWebLink1());
        Assertions.assertThat(response.getWebLink2()).isEqualTo(clubCreateRequestDTO.getWebLink2());
        Assertions.assertThat(response.getLogo().getFileName()).isNotEqualTo("alt.jpg");
        Assertions.assertThat(response.getLogo().getBytes()).isNotNull();
        Assertions.assertThat(response.getLogo().getId()).isNotNull();
    }

    @Test
    public void getClubByName_WithRecruit() throws Exception {
        //given
        int recruitIndex = 2;

        //when
        ClubResponseDTO response = clubController.getClubByName("testClubName2").getBody();

        //then
        ClubCreateRequestDTO clubCreateRequestDTO = testDataRepository.getClubCreateRequestDTO(2);
        Assertions.assertThat(response.getName()).isEqualTo(clubCreateRequestDTO.getClubName());
        Assertions.assertThat(response.getActivityDescription()).isEqualTo(clubCreateRequestDTO.getActivityDescription());
        Assertions.assertThat(response.getBriefActivityDescription()).isEqualTo(clubCreateRequestDTO.getBriefActivityDescription());
        Assertions.assertThat(response.getClubDescription()).isEqualTo(clubCreateRequestDTO.getClubDescription());
        Assertions.assertThat(response.getBelongs()).isEqualTo(clubCreateRequestDTO.getBelongs());
        Assertions.assertThat(response.getCampus()).isEqualTo(clubCreateRequestDTO.getCampus());
        Assertions.assertThat(response.getClubType()).isEqualTo(clubCreateRequestDTO.getClubType());
        Assertions.assertThat(response.getEstablishAt()).isEqualTo(clubCreateRequestDTO.getEstablishDate());
        Assertions.assertThat(response.getHeadLine()).isEqualTo(clubCreateRequestDTO.getHeadLine());
        Assertions.assertThat(response.getMandatoryActivatePeriod()).isEqualTo(clubCreateRequestDTO.getMandatoryActivatePeriod());
        Assertions.assertThat(response.getMemberAmount()).isEqualTo(clubCreateRequestDTO.getMemberAmount());
        Assertions.assertThat(response.getRegularMeetingTime()).isEqualTo(clubCreateRequestDTO.getRegularMeetingTime());
        Assertions.assertThat(response.getRoomLocation()).isEqualTo(clubCreateRequestDTO.getRoomLocation());
        Assertions.assertThat(response.getWebLink1()).isEqualTo(clubCreateRequestDTO.getWebLink1());
        Assertions.assertThat(response.getWebLink2()).isEqualTo(clubCreateRequestDTO.getWebLink2());

        Assertions.assertThat(response.getRecruit()).isNotEmpty();
        response.getRecruit().ifPresent(
                recruit -> {
                    Assertions.assertThat(recruit.getRecruitStartAt()).isNotNull();
                    Assertions.assertThat(recruit.getRecruitEndAt()).isNotNull();
                    Assertions.assertThat(recruit.getRecruitQuota()).contains(String.valueOf(recruitIndex));
                    Assertions.assertThat(recruit.getRecruitProcessDescription()).contains(String.valueOf(recruitIndex));
                    Assertions.assertThat(recruit.getRecruitContact()).contains(String.valueOf(recruitIndex));
                    Assertions.assertThat(recruit.getRecruitWebLink()).contains(String.valueOf(recruitIndex));

                }
        );
    }

    @Test
    public void getClubByName_AlwaysRecruit() throws Exception {
        //given
        int recruitIndex = 1;

        //when
        ClubResponseDTO response = clubController.getClubByName("testClubName1").getBody();

        //then
        ClubCreateRequestDTO clubCreateRequestDTO = testDataRepository.getClubCreateRequestDTO(1);
        Assertions.assertThat(response.getName()).isEqualTo(clubCreateRequestDTO.getClubName());
        Assertions.assertThat(response.getActivityDescription()).isEqualTo(clubCreateRequestDTO.getActivityDescription());
        Assertions.assertThat(response.getBriefActivityDescription()).isEqualTo(clubCreateRequestDTO.getBriefActivityDescription());
        Assertions.assertThat(response.getClubDescription()).isEqualTo(clubCreateRequestDTO.getClubDescription());
        Assertions.assertThat(response.getBelongs()).isEqualTo(clubCreateRequestDTO.getBelongs());
        Assertions.assertThat(response.getCampus()).isEqualTo(clubCreateRequestDTO.getCampus());
        Assertions.assertThat(response.getClubType()).isEqualTo(clubCreateRequestDTO.getClubType());
        Assertions.assertThat(response.getEstablishAt()).isEqualTo(clubCreateRequestDTO.getEstablishDate());
        Assertions.assertThat(response.getHeadLine()).isEqualTo(clubCreateRequestDTO.getHeadLine());
        Assertions.assertThat(response.getMandatoryActivatePeriod()).isEqualTo(clubCreateRequestDTO.getMandatoryActivatePeriod());
        Assertions.assertThat(response.getMemberAmount()).isEqualTo(clubCreateRequestDTO.getMemberAmount());
        Assertions.assertThat(response.getRegularMeetingTime()).isEqualTo(clubCreateRequestDTO.getRegularMeetingTime());
        Assertions.assertThat(response.getRoomLocation()).isEqualTo(clubCreateRequestDTO.getRoomLocation());
        Assertions.assertThat(response.getWebLink1()).isEqualTo(clubCreateRequestDTO.getWebLink1());
        Assertions.assertThat(response.getWebLink2()).isEqualTo(clubCreateRequestDTO.getWebLink2());

        Assertions.assertThat(response.getRecruit()).isNotEmpty();
        response.getRecruit().ifPresent(
                recruit -> {
                    Assertions.assertThat(recruit.getRecruitStartAt()).isNull();
                    Assertions.assertThat(recruit.getRecruitEndAt()).isNull();
                    Assertions.assertThat(recruit.getRecruitQuota()).contains(String.valueOf(recruitIndex));
                    Assertions.assertThat(recruit.getRecruitProcessDescription()).contains(String.valueOf(recruitIndex));
                    Assertions.assertThat(recruit.getRecruitContact()).contains(String.valueOf(recruitIndex));
                    Assertions.assertThat(recruit.getRecruitWebLink()).contains(String.valueOf(recruitIndex));

                }
        );
    }

    @Test
    public void getClubByName_NoRecruit() throws Exception {
        //given

        //when
        ClubResponseDTO response = clubController.getClubByName("testClubName0").getBody();

        //then
        ClubCreateRequestDTO clubCreateRequestDTO = testDataRepository.getClubCreateRequestDTO(0);
        Assertions.assertThat(response.getName()).isEqualTo(clubCreateRequestDTO.getClubName());
        Assertions.assertThat(response.getActivityDescription()).isEqualTo(clubCreateRequestDTO.getActivityDescription());
        Assertions.assertThat(response.getBriefActivityDescription()).isEqualTo(clubCreateRequestDTO.getBriefActivityDescription());
        Assertions.assertThat(response.getClubDescription()).isEqualTo(clubCreateRequestDTO.getClubDescription());
        Assertions.assertThat(response.getBelongs()).isEqualTo(clubCreateRequestDTO.getBelongs());
        Assertions.assertThat(response.getCampus()).isEqualTo(clubCreateRequestDTO.getCampus());
        Assertions.assertThat(response.getClubType()).isEqualTo(clubCreateRequestDTO.getClubType());
        Assertions.assertThat(response.getEstablishAt()).isEqualTo(clubCreateRequestDTO.getEstablishDate());
        Assertions.assertThat(response.getHeadLine()).isEqualTo(clubCreateRequestDTO.getHeadLine());
        Assertions.assertThat(response.getMandatoryActivatePeriod()).isEqualTo(clubCreateRequestDTO.getMandatoryActivatePeriod());
        Assertions.assertThat(response.getMemberAmount()).isEqualTo(clubCreateRequestDTO.getMemberAmount());
        Assertions.assertThat(response.getRegularMeetingTime()).isEqualTo(clubCreateRequestDTO.getRegularMeetingTime());
        Assertions.assertThat(response.getRoomLocation()).isEqualTo(clubCreateRequestDTO.getRoomLocation());
        Assertions.assertThat(response.getWebLink1()).isEqualTo(clubCreateRequestDTO.getWebLink1());
        Assertions.assertThat(response.getWebLink2()).isEqualTo(clubCreateRequestDTO.getWebLink2());

        Assertions.assertThat(response.getRecruit()).isEmpty();
    }

    @Test
    public void getClubByName_ManyActImg() throws Exception {
        //given

        //when
        ClubResponseDTO response = clubController.getClubByName("testClubName5").getBody();

        //then
        ClubCreateRequestDTO clubCreateRequestDTO = testDataRepository.getClubCreateRequestDTO(5);
        Assertions.assertThat(response.getName()).isEqualTo(clubCreateRequestDTO.getClubName());
        Assertions.assertThat(response.getActivityDescription()).isEqualTo(clubCreateRequestDTO.getActivityDescription());
        Assertions.assertThat(response.getBriefActivityDescription()).isEqualTo(clubCreateRequestDTO.getBriefActivityDescription());
        Assertions.assertThat(response.getClubDescription()).isEqualTo(clubCreateRequestDTO.getClubDescription());
        Assertions.assertThat(response.getBelongs()).isEqualTo(clubCreateRequestDTO.getBelongs());
        Assertions.assertThat(response.getCampus()).isEqualTo(clubCreateRequestDTO.getCampus());
        Assertions.assertThat(response.getClubType()).isEqualTo(clubCreateRequestDTO.getClubType());
        Assertions.assertThat(response.getEstablishAt()).isEqualTo(clubCreateRequestDTO.getEstablishDate());
        Assertions.assertThat(response.getHeadLine()).isEqualTo(clubCreateRequestDTO.getHeadLine());
        Assertions.assertThat(response.getMandatoryActivatePeriod()).isEqualTo(clubCreateRequestDTO.getMandatoryActivatePeriod());
        Assertions.assertThat(response.getMemberAmount()).isEqualTo(clubCreateRequestDTO.getMemberAmount());
        Assertions.assertThat(response.getRegularMeetingTime()).isEqualTo(clubCreateRequestDTO.getRegularMeetingTime());
        Assertions.assertThat(response.getRoomLocation()).isEqualTo(clubCreateRequestDTO.getRoomLocation());
        Assertions.assertThat(response.getWebLink1()).isEqualTo(clubCreateRequestDTO.getWebLink1());
        Assertions.assertThat(response.getWebLink2()).isEqualTo(clubCreateRequestDTO.getWebLink2());

        List<S3DownloadDto> activityImages = response.getActivityImages();
        Assertions.assertThat(activityImages).hasSize(5);
        activityImages.stream()
                .forEach(
                        s3DownloadDto -> {
                            Assertions.assertThat(s3DownloadDto.getId()).isNotNull();
                            Assertions.assertThat(s3DownloadDto.getBytes()).isNotNull();
                            Assertions.assertThat(s3DownloadDto.getFileName()).isEqualTo("4.jpg");
                        }
                );
    }

    @Test
    public void getClubByName_OneActImg() throws Exception {
        //given

        //when
        ClubResponseDTO response = clubController.getClubByName("testClubName1").getBody();

        //then
        ClubCreateRequestDTO clubCreateRequestDTO = testDataRepository.getClubCreateRequestDTO(1);
        Assertions.assertThat(response.getName()).isEqualTo(clubCreateRequestDTO.getClubName());
        Assertions.assertThat(response.getActivityDescription()).isEqualTo(clubCreateRequestDTO.getActivityDescription());
        Assertions.assertThat(response.getBriefActivityDescription()).isEqualTo(clubCreateRequestDTO.getBriefActivityDescription());
        Assertions.assertThat(response.getClubDescription()).isEqualTo(clubCreateRequestDTO.getClubDescription());
        Assertions.assertThat(response.getBelongs()).isEqualTo(clubCreateRequestDTO.getBelongs());
        Assertions.assertThat(response.getCampus()).isEqualTo(clubCreateRequestDTO.getCampus());
        Assertions.assertThat(response.getClubType()).isEqualTo(clubCreateRequestDTO.getClubType());
        Assertions.assertThat(response.getEstablishAt()).isEqualTo(clubCreateRequestDTO.getEstablishDate());
        Assertions.assertThat(response.getHeadLine()).isEqualTo(clubCreateRequestDTO.getHeadLine());
        Assertions.assertThat(response.getMandatoryActivatePeriod()).isEqualTo(clubCreateRequestDTO.getMandatoryActivatePeriod());
        Assertions.assertThat(response.getMemberAmount()).isEqualTo(clubCreateRequestDTO.getMemberAmount());
        Assertions.assertThat(response.getRegularMeetingTime()).isEqualTo(clubCreateRequestDTO.getRegularMeetingTime());
        Assertions.assertThat(response.getRoomLocation()).isEqualTo(clubCreateRequestDTO.getRoomLocation());
        Assertions.assertThat(response.getWebLink1()).isEqualTo(clubCreateRequestDTO.getWebLink1());
        Assertions.assertThat(response.getWebLink2()).isEqualTo(clubCreateRequestDTO.getWebLink2());

        List<S3DownloadDto> activityImages = response.getActivityImages();
        Assertions.assertThat(activityImages).hasSize(1);
        activityImages.stream()
                .forEach(
                        s3DownloadDto -> {
                            Assertions.assertThat(s3DownloadDto.getId()).isNotNull();
                            Assertions.assertThat(s3DownloadDto.getBytes()).isNotNull();
                            Assertions.assertThat(s3DownloadDto.getFileName()).isEqualTo("4.jpg");
                        }
                );
    }

    @Test
    public void getClubByName_NoActImg() throws Exception {
        //given

        //when
        ClubResponseDTO response = clubController.getClubByName("testClubName0").getBody();

        //then
        ClubCreateRequestDTO clubCreateRequestDTO = testDataRepository.getClubCreateRequestDTO(0);
        Assertions.assertThat(response.getName()).isEqualTo(clubCreateRequestDTO.getClubName());
        Assertions.assertThat(response.getActivityDescription()).isEqualTo(clubCreateRequestDTO.getActivityDescription());
        Assertions.assertThat(response.getBriefActivityDescription()).isEqualTo(clubCreateRequestDTO.getBriefActivityDescription());
        Assertions.assertThat(response.getClubDescription()).isEqualTo(clubCreateRequestDTO.getClubDescription());
        Assertions.assertThat(response.getBelongs()).isEqualTo(clubCreateRequestDTO.getBelongs());
        Assertions.assertThat(response.getCampus()).isEqualTo(clubCreateRequestDTO.getCampus());
        Assertions.assertThat(response.getClubType()).isEqualTo(clubCreateRequestDTO.getClubType());
        Assertions.assertThat(response.getEstablishAt()).isEqualTo(clubCreateRequestDTO.getEstablishDate());
        Assertions.assertThat(response.getHeadLine()).isEqualTo(clubCreateRequestDTO.getHeadLine());
        Assertions.assertThat(response.getMandatoryActivatePeriod()).isEqualTo(clubCreateRequestDTO.getMandatoryActivatePeriod());
        Assertions.assertThat(response.getMemberAmount()).isEqualTo(clubCreateRequestDTO.getMemberAmount());
        Assertions.assertThat(response.getRegularMeetingTime()).isEqualTo(clubCreateRequestDTO.getRegularMeetingTime());
        Assertions.assertThat(response.getRoomLocation()).isEqualTo(clubCreateRequestDTO.getRoomLocation());
        Assertions.assertThat(response.getWebLink1()).isEqualTo(clubCreateRequestDTO.getWebLink1());
        Assertions.assertThat(response.getWebLink2()).isEqualTo(clubCreateRequestDTO.getWebLink2());

        List<S3DownloadDto> activityImages = response.getActivityImages();
        Assertions.assertThat(activityImages).isEmpty();
    }


    /**
     명륜 - 중앙동아리 - 취미교양
     명륜 - 준중앙동아리 - 봉사
     율전 - 중앙동아리 - 과학기술
     율전 - 준중앙동아리 - 건강체육
     */
    /**
     * select club_id
     * from club
     * where
     * campus = '명륜'
     * and club_type = '중앙동아리'
     * and belongs = '취미교양' order by name;
     */
    @Test
    public void getClubPrevByCategories_FullCategory() throws Exception {
        //given
        Campus campus = Campus.명륜;
        ClubType clubType = ClubType.중앙동아리;
        String belongs = "취미교양";
        PageRequest page = PageRequest.of(1, 3);

        //when
        Page<ClubPrevResponseDTO> prevPages = clubController.getClubPrevByCategories(campus, clubType, belongs, page);

        //then
        Assertions.assertThat(prevPages.getTotalElements()).isEqualTo(4);
        Assertions.assertThat(prevPages.getTotalPages()).isEqualTo(2);
        Assertions.assertThat(prevPages.getNumber()).isEqualTo(1);
        Assertions.assertThat(prevPages.getNumberOfElements()).isEqualTo(1);
        List<ClubPrevResponseDTO> prevs = prevPages.getContent();
        Assertions.assertThat(prevs.size()).isEqualTo(1);
        Assertions.assertThat(prevs.stream().map(ClubPrevResponseDTO::getId).collect(Collectors.toList()))
                .containsExactly(8L);
        prevs.stream()
                .forEach(
                        dto -> {
                            Assertions.assertThat(dto.getBelongs()).isEqualTo(belongs);
                            Assertions.assertThat(dto.getLogo()).isNotNull();
                            Assertions.assertThat(dto.getLogo().getBytes()).isNotNull();
                            Assertions.assertThat(dto.getLogo().getFileName()).isEqualTo("3.jpg");
                        }
                );

    }

    @Test
    public void getClubPrevByCategories_NoBelongs() throws Exception {
        //given
        Campus campus = Campus.명륜;
        ClubType clubType = ClubType.중앙동아리;
        PageRequest page = PageRequest.of(1, 3);

        //when
        Page<ClubPrevResponseDTO> prevPages = clubController.getClubPrevByCategories(campus, clubType, "전체", page);

        //then
        Assertions.assertThat(prevPages.getTotalElements()).isEqualTo(8);
        Assertions.assertThat(prevPages.getTotalPages()).isEqualTo(3);
        Assertions.assertThat(prevPages.getSize()).isEqualTo(3);
        Assertions.assertThat(prevPages.getNumberOfElements()).isEqualTo(3);
        Assertions.assertThat(prevPages.getNumber()).isEqualTo(1);
        List<ClubPrevResponseDTO> prevs = prevPages.getContent();
        Assertions.assertThat(prevs.stream().map(ClubPrevResponseDTO::getId).collect(Collectors.toList()))
                .containsExactly(24L, 31L, 32L);
        prevs.stream()
                .forEach(
                        dto -> {
                            Assertions.assertThat(dto.getLogo()).isNotNull();
                            Assertions.assertThat(dto.getLogo().getBytes()).isNotNull();
                            Assertions.assertThat(dto.getLogo().getFileName()).contains(".jpg");
                        }
                );

    }

    @Test
    public void getClubPrevByCategories_NoClubType() throws Exception {
        //given
        Campus campus = Campus.명륜;

        //when
        Page<ClubPrevResponseDTO> prevPages = clubController.getClubPrevByCategories(campus, ClubType.전체, "전체", null);

        //then
        Assertions.assertThat(prevPages.getTotalElements()).isEqualTo(16);
        Assertions.assertThat(prevPages.getTotalPages()).isEqualTo(1);
        Assertions.assertThat(prevPages.getSize()).isEqualTo(16);
        Assertions.assertThat(prevPages.getNumberOfElements()).isEqualTo(16);
        Assertions.assertThat(prevPages.getNumber()).isEqualTo(0);
        List<ClubPrevResponseDTO> prevs = prevPages.getContent();
        Assertions.assertThat(prevs.stream().map(ClubPrevResponseDTO::getId).collect(Collectors.toList()))
                .containsExactly(13L, 14L, 15L, 16L,
                        21L, 22L, 23L, 24L, 29L,
                        30L, 31L, 32L, 5L, 6L, 7L, 8L);
        prevs.stream()
                .forEach(
                        dto -> {
                            Assertions.assertThat(dto.getLogo()).isNotNull();
                            Assertions.assertThat(dto.getLogo().getBytes()).isNotNull();
                            Assertions.assertThat(dto.getLogo().getFileName()).contains(".jpg");
                        }
                );
    }

    @Test
    public void getClubPrevByKeyword_ExactlyOneMatch() throws Exception {
        //given
        String keyword = "testClubName0";
        PageRequest pageRequest = PageRequest.of(0, 10);

        //when
        Page<ClubPrevResponseDTO> prevPages = clubController.getClubPrevByKeyword(keyword, pageRequest);

        //then
        Assertions.assertThat(prevPages.getTotalElements()).isEqualTo(1);
        Assertions.assertThat(prevPages.getTotalPages()).isEqualTo(1);
        Assertions.assertThat(prevPages.getSize()).isEqualTo(10);
        Assertions.assertThat(prevPages.getNumberOfElements()).isEqualTo(1);
        Assertions.assertThat(prevPages.getNumber()).isEqualTo(0);
        List<ClubPrevResponseDTO> prevs = prevPages.getContent();
        Assertions.assertThat(prevs.stream().map(ClubPrevResponseDTO::getId).collect(Collectors.toList()))
                .containsExactly(1L);
        prevs.stream()
                .forEach(
                        dto -> {
                            Assertions.assertThat(dto.getLogo()).isNotNull();
                            Assertions.assertThat(dto.getLogo().getBytes()).isNotNull();
                            Assertions.assertThat(dto.getLogo().getFileName()).contains(".jpg");
                        }
                );
    }

    @Test
    public void getClubPrevByKeyword_middleManyMatch() throws Exception {
        //given
        String keyword = "2";
        PageRequest pageRequest = PageRequest.of(0, 3);

        //when
        Page<ClubPrevResponseDTO> prevPages = clubController.getClubPrevByKeyword(keyword, pageRequest);

        //then
        Assertions.assertThat(prevPages.getTotalElements()).isEqualTo(13);
        Assertions.assertThat(prevPages.getTotalPages()).isEqualTo(5);
        Assertions.assertThat(prevPages.getSize()).isEqualTo(3);
        Assertions.assertThat(prevPages.getNumberOfElements()).isEqualTo(3);
        Assertions.assertThat(prevPages.getNumber()).isEqualTo(0);
        List<ClubPrevResponseDTO> prevs = prevPages.getContent();
        Assertions.assertThat(prevs.stream().map(ClubPrevResponseDTO::getId).collect(Collectors.toList()))
                .containsExactly(13L, 3L, 21L);
        prevs.stream()
                .forEach(
                        dto -> {
                            Assertions.assertThat(dto.getLogo()).isNotNull();
                            Assertions.assertThat(dto.getLogo().getBytes()).isNotNull();
                            Assertions.assertThat(dto.getLogo().getFileName()).contains(".jpg");
                        }
                );
    }

    @Test
    public void getClubPrevByKeyword_NoMatch() throws Exception {
        //given
        String keyword = "NoMatch";

        //when
        Page<ClubPrevResponseDTO> prevPages = clubController.getClubPrevByKeyword(keyword, null);

        //then
        Assertions.assertThat(prevPages).isEmpty();
    }

    @Test
    public void getRandomClubNameAndIdByCategories_FullCategory() throws Exception {
        //given
        Campus campus = Campus.명륜;
        ClubType clubType = ClubType.중앙동아리;
        String belongs = "취미교양";

        //when
        List<RandomClubsResponse> randomPrevs1 = clubController.getRandomClubNameAndIdByCategories(campus, clubType, belongs);
        List<RandomClubsResponse> randomPrevs2 = clubController.getRandomClubNameAndIdByCategories(campus, clubType, belongs);
        List<RandomClubsResponse> randomPrevs3 = clubController.getRandomClubNameAndIdByCategories(campus, clubType, belongs);

        //then
        Assertions.assertThat(randomPrevs1).hasSize(3);
        Assertions.assertThat(randomPrevs2).hasSize(3);
        Assertions.assertThat(randomPrevs3).hasSize(3);
        boolean r = true;
        for (int i = 0; i < 3; i++) {
            boolean a = randomPrevs1.get(i).getId() == randomPrevs2.get(i).getId();
            boolean b = randomPrevs2.get(i).getId() == randomPrevs3.get(i).getId();
            boolean c = randomPrevs3.get(i).getId() == randomPrevs1.get(i).getId();
            r = r && (a && b && c);
        }
        assertFalse(r);
    }

    @Test
    public void getRandomClubNameAndIdByCategories_NoBelongs() throws Exception {
        //given
        Campus campus = Campus.명륜;
        ClubType clubType = ClubType.중앙동아리;

        //when
        List<RandomClubsResponse> randomPrevs1 = clubController.getRandomClubNameAndIdByCategories(campus, clubType, "전체");
        List<RandomClubsResponse> randomPrevs2 = clubController.getRandomClubNameAndIdByCategories(campus, clubType, "전체");
        List<RandomClubsResponse> randomPrevs3 = clubController.getRandomClubNameAndIdByCategories(campus, clubType, "전체");

        //then
        Assertions.assertThat(randomPrevs1).hasSize(3);
        Assertions.assertThat(randomPrevs2).hasSize(3);
        Assertions.assertThat(randomPrevs3).hasSize(3);
        for (int i = 0; i < 3; i++) {
            boolean a = randomPrevs1.get(i).getId() == randomPrevs2.get(i).getId();
            boolean b = randomPrevs2.get(i).getId() == randomPrevs3.get(i).getId();
            boolean c = randomPrevs3.get(i).getId() == randomPrevs1.get(i).getId();
            assertFalse(a && b && c);
        }
    }

    @Test
    public void getRandomClubNameAndIdByCategories_NoClubType() throws Exception {
        //given
        Campus campus = Campus.명륜;

        //when
        List<RandomClubsResponse> randomPrevs1 = clubController.getRandomClubNameAndIdByCategories(campus, ClubType.전체, "전체");
        List<RandomClubsResponse> randomPrevs2 = clubController.getRandomClubNameAndIdByCategories(campus, ClubType.전체, "전체");
        List<RandomClubsResponse> randomPrevs3 = clubController.getRandomClubNameAndIdByCategories(campus, ClubType.전체, "전체");

        //then
        Assertions.assertThat(randomPrevs1).hasSize(3);
        Assertions.assertThat(randomPrevs2).hasSize(3);
        Assertions.assertThat(randomPrevs3).hasSize(3);
        for (int i = 0; i < 3; i++) {
            boolean a = randomPrevs1.get(i).getId() == randomPrevs2.get(i).getId();
            boolean b = randomPrevs2.get(i).getId() == randomPrevs3.get(i).getId();
            boolean c = randomPrevs3.get(i).getId() == randomPrevs1.get(i).getId();
            assertFalse(a && b && c);
        }
    }
}
