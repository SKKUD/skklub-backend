package com.skklub.admin.controller.club;

import akka.protobuf.WireFormat;
import com.skklub.admin.TestDataRepository;
import com.skklub.admin.controller.AuthValidator;
import com.skklub.admin.controller.ClubController;
import com.skklub.admin.controller.RestDocsUtils;
import com.skklub.admin.controller.S3Transferer;
import com.skklub.admin.controller.dto.RecruitDto;
import com.skklub.admin.controller.dto.S3DownloadDto;
import com.skklub.admin.domain.Club;
import com.skklub.admin.domain.User;
import com.skklub.admin.domain.enums.Campus;
import com.skklub.admin.domain.enums.ClubType;
import com.skklub.admin.domain.enums.Role;
import com.skklub.admin.exception.deprecated.error.exception.AdminCannotHaveClubException;
import com.skklub.admin.exception.deprecated.error.exception.ClubIdMisMatchException;
import com.skklub.admin.exception.deprecated.error.exception.ClubNameMisMatchException;
import com.skklub.admin.exception.deprecated.error.exception.InvalidBelongsException;
import com.skklub.admin.repository.ClubRepository;
import com.skklub.admin.repository.UserRepository;
import com.skklub.admin.service.ClubService;
import com.skklub.admin.service.dto.ClubDetailInfoDto;
import com.skklub.admin.service.dto.FileNames;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.skklub.admin.controller.RestDocsUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@WithMockUser
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import(TestDataRepository.class)
@WebMvcTest(controllers = ClubController.class)
@MockBean(JpaMetamodelMappingContext.class)
class ClubControllerReadTest {
    @MockBean
    private UserRepository userRepository;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ClubService clubService;
    @MockBean
    private ClubRepository clubRepository;
    @MockBean
    private S3Transferer s3Transferer;
    @MockBean
    private AuthValidator authValidator;
    @InjectMocks
    private TestDataRepository testDataRepository;

    @BeforeEach
    public void beforeEach() {
        doNothing().when(authValidator).validateUpdatingClub(anyLong());
        doNothing().when(authValidator).validateUpdatingNotice(anyLong());
        doNothing().when(authValidator).validateUpdatingUser(anyLong());
        doNothing().when(authValidator).validatePendingRequestAuthority(anyLong());
    }

    @Test
    public void getClubById_Default_Success() throws Exception{
        //given
        long clubId = 0L;
        Club club = testDataRepository.getClubs().get((int) clubId);
        Field id = club.getClass().getDeclaredField("id");
        id.setAccessible(true);
        id.set(club, clubId);
        ClubDetailInfoDto clubDetailInfoDto = testDataRepository.getClubDetailInfoDtos().get((int) clubId);
        S3DownloadDto logoS3DownloadDto = testDataRepository.getLogoS3DownloadDto((int) clubId);
        List<S3DownloadDto> activityImgS3DownloadDtos = testDataRepository.getActivityImgS3DownloadDtos((int) clubId);
        given(clubRepository.findDetailClubById(clubId)).willReturn(Optional.of(club));
        given(s3Transferer.downloadOne(clubDetailInfoDto.getLogo())).willReturn(logoS3DownloadDto);
        given(s3Transferer.downloadAll(clubDetailInfoDto.getActivityImages())).willReturn(activityImgS3DownloadDtos);
        //when
        ResultActions actions = mockMvc.perform(
                get("/club/{clubId}", clubId)
                        .with(csrf())
        );

        //then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clubDetailInfoDto.getId()))
                .andExpect(jsonPath("$.campus").value(clubDetailInfoDto.getCampus().toString()))
                .andExpect(jsonPath("$.clubType").value(clubDetailInfoDto.getClubType().toString()))
                .andExpect(jsonPath("$.belongs").value(clubDetailInfoDto.getBelongs()))
                .andExpect(jsonPath("$.briefActivityDescription").value(clubDetailInfoDto.getBriefActivityDescription()))
                .andExpect(jsonPath("$.name").value(clubDetailInfoDto.getName()))
                .andExpect(jsonPath("$.headLine").value(clubDetailInfoDto.getHeadLine()))
                .andExpect(jsonPath("$.establishAt").value(clubDetailInfoDto.getEstablishAt()))
                .andExpect(jsonPath("$.roomLocation").value(clubDetailInfoDto.getRoomLocation()))
                .andExpect(jsonPath("$.memberAmount").value(clubDetailInfoDto.getMemberAmount()))
                .andExpect(jsonPath("$.regularMeetingTime").value(clubDetailInfoDto.getRegularMeetingTime()))
                .andExpect(jsonPath("$.mandatoryActivatePeriod").value(clubDetailInfoDto.getMandatoryActivatePeriod()))
                .andExpect(jsonPath("$.clubDescription").value(clubDetailInfoDto.getClubDescription()))
                .andExpect(jsonPath("$.activityDescription").value(clubDetailInfoDto.getActivityDescription()))
                .andExpect(jsonPath("$.webLink1").value(clubDetailInfoDto.getWebLink1()))
                .andExpect(jsonPath("$.webLink2").value(clubDetailInfoDto.getWebLink2()))
                .andExpect(jsonPath("$.presidentName").value(clubDetailInfoDto.getPresidentName()))
                .andExpect(jsonPath("$.presidentContact").value(clubDetailInfoDto.getPresidentContact()))
                .andExpect(jsonPath("$.logo.id").value(logoS3DownloadDto.getId()))
                .andExpect(jsonPath("$.logo.fileName").value(logoS3DownloadDto.getFileName()))
                .andExpect(jsonPath("$.logo.url").value(logoS3DownloadDto.getUrl()));
                clubDetailInfoDto.getRecruit().ifPresent(r -> {
                    try {
                        checkRecruitResponseJson(actions, r);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                for(int i = 0; i < testDataRepository.getActivityImgPerClub(); i++) {
                    checkActivityImagesResponseJson(actions, i, activityImgS3DownloadDtos);
                }
        actions.andDo(
                document("club/get/detail/id",
                        pathParameters(
                                parameterWithName("clubId").attributes(example("1")).description("동아리 ID")
                        ),
                        responseFields(
                                fieldWithPath("id").type(WireFormat.FieldType.INT64).description("동아리 Id").attributes(example(clubDetailInfoDto.getId().toString())),
                                fieldWithPath("campus").type(WireFormat.FieldType.STRING).description("분류 - 캠퍼스").attributes(example(clubDetailInfoDto.getCampus().toString())),
                                fieldWithPath("clubType").type(WireFormat.FieldType.STRING).description("분류 - 동아리 종류").attributes(example(clubDetailInfoDto.getClubType().toString())),
                                fieldWithPath("belongs").type(WireFormat.FieldType.STRING).description("분류 - 동아리 분과").attributes(example(clubDetailInfoDto.getBelongs())),
                                fieldWithPath("briefActivityDescription").type(WireFormat.FieldType.STRING).description("분류 - 활동 설명").attributes(example(clubDetailInfoDto.getBriefActivityDescription())),
                                fieldWithPath("name").type(WireFormat.FieldType.STRING).description("동아리 이름").attributes(example(clubDetailInfoDto.getName())),
                                fieldWithPath("headLine").type(WireFormat.FieldType.STRING).description("한줄 소개").attributes(example(clubDetailInfoDto.getHeadLine())),
                                fieldWithPath("establishAt").type(WireFormat.FieldType.STRING).description("설립 연도").attributes(example(clubDetailInfoDto.getEstablishAt().toString())),
                                fieldWithPath("roomLocation").type(WireFormat.FieldType.STRING).description("동아리 방 위치").attributes(example(clubDetailInfoDto.getRoomLocation())),
                                fieldWithPath("memberAmount").type(WireFormat.FieldType.STRING).description("동아리 인원").attributes(example(clubDetailInfoDto.getMemberAmount().toString())),
                                fieldWithPath("regularMeetingTime").type(WireFormat.FieldType.STRING).description("정규 모임 시간").attributes(example(clubDetailInfoDto.getRegularMeetingTime())),
                                fieldWithPath("mandatoryActivatePeriod").type(WireFormat.FieldType.STRING).description("의무 활동 기간").attributes(example(clubDetailInfoDto.getMandatoryActivatePeriod())),
                                fieldWithPath("clubDescription").type(WireFormat.FieldType.STRING).description("자세한 동아리 설명").attributes(example(clubDetailInfoDto.getClubDescription())),
                                fieldWithPath("activityDescription").type(WireFormat.FieldType.STRING).description("자세한 활동 설명").attributes(example(clubDetailInfoDto.getActivityDescription())),
                                fieldWithPath("webLink1").type(WireFormat.FieldType.STRING).description("관련 사이트 주소 1").attributes(example(clubDetailInfoDto.getWebLink1())),
                                fieldWithPath("webLink2").type(WireFormat.FieldType.STRING).description("관련 사이트 주소 2").attributes(example(clubDetailInfoDto.getWebLink2())),
                                fieldWithPath("recruit.recruitStartAt").type(WireFormat.FieldType.STRING).description("모집 시작일 (* 상시 모집의 경우엔 NULL)").attributes(example("yyyy-MM-ddTHH:mm (null when not recruting)")),
                                fieldWithPath("recruit.recruitEndAt").type(WireFormat.FieldType.STRING).description("모집 종료일 (* 상시 모집의 경우엔 NULL)").attributes(example("yyyy-MM-ddTHH:mm (null when not recruting)")),
                                fieldWithPath("recruit.recruitQuota").type(WireFormat.FieldType.STRING).description("모집 인원").attributes(example("10 ~ 30명 - Can Any Format(null when not recruting)")),
                                fieldWithPath("recruit.recruitProcessDescription").type(WireFormat.FieldType.STRING).description("모집 절차 설명").attributes(example("Test Recruit Format(null when not recruting)")),
                                fieldWithPath("recruit.recruitContact").type(WireFormat.FieldType.STRING).description("모집 문의처").attributes(example("010-1234-1234 or recruit@asd.asd - Can Any Format(null when not recruting)")),
                                fieldWithPath("recruit.recruitWebLink").type(WireFormat.FieldType.STRING).description("모집 링크").attributes(example("form.goole.com - Can Any Format(null when not recruting)")),
                                fieldWithPath("presidentName").type(WireFormat.FieldType.STRING).description("회장 이름").attributes(example(clubDetailInfoDto.getPresidentName())),
                                fieldWithPath("presidentContact").type(WireFormat.FieldType.STRING).description("회장 연락처").attributes(example(clubDetailInfoDto.getPresidentContact())),
                                fieldWithPath("logo.id").type(WireFormat.FieldType.INT64).description("로고 ID").attributes(example(logoS3DownloadDto.getId().toString())),
                                fieldWithPath("logo.fileName").type(WireFormat.FieldType.STRING).description("로고 파일명").attributes(example(logoS3DownloadDto.getFileName())),
                                fieldWithPath("logo.url").type(WireFormat.FieldType.STRING).description("로고 리소스 주소").attributes(example("https://s3.ap-northeast-2.amazonaws.com/skklub.test/024f3d7b-0ae0-4011-8f3f-23637d10f3d4.jpg")),
                                fieldWithPath("activityImages[].id").type(WireFormat.FieldType.STRING).description("활동 사진 ID").attributes(example(activityImgS3DownloadDtos.get(0).getId().toString())),
                                fieldWithPath("activityImages[].fileName").type(WireFormat.FieldType.STRING).description("활동 사진 파일명").attributes(example(activityImgS3DownloadDtos.get(0).getFileName())),
                                fieldWithPath("activityImages.[]url").type(WireFormat.FieldType.STRING).description("활동 사진 리소스 주소") .attributes(example("https://s3.ap-northeast-2.amazonaws.com/skklub.test/024f3d7b-0ae0-4011-8f3f-23637d10f3d4.jpg"))
                        )
                )
        );
     }

    @Test
    public void getClubById_IllegalClubId_ClubIdMisMatchException() throws Exception{
         //given
         Long clubId = -1L;
         given(clubRepository.findDetailClubById(clubId)).willReturn(Optional.empty());

         //when
         MvcResult badIdResult = mockMvc.perform(
                 get("/club/{clubId}", clubId)
                         .with(csrf())
         ).andExpect(status().isBadRequest()).andReturn();

         //then
         assertThat(badIdResult.getResolvedException()).isExactlyInstanceOf(ClubIdMisMatchException.class);

      }

    @Test
    @WithMockUser
    public void getMyClubByLoginUser_Default_Success() throws Exception{
        //given
        long clubId = 0L;
        Club club = testDataRepository.getClubs().get((int) clubId);
        Field id = club.getClass().getDeclaredField("id");
        id.setAccessible(true);
        id.set(club, clubId);
        ClubDetailInfoDto clubDetailInfoDto = testDataRepository.getClubDetailInfoDtos().get((int) clubId);
        S3DownloadDto logoS3DownloadDto = testDataRepository.getLogoS3DownloadDto((int) clubId);
        List<S3DownloadDto> activityImgS3DownloadDtos = testDataRepository.getActivityImgS3DownloadDtos((int) clubId);

        String username = "user";
        User user = new User(username, null, Role.ROLE_USER, null, null);
        given(userRepository.findByUsername(username)).willReturn(user);
        given(clubRepository.findDetailClubByPresident(user)).willReturn(Optional.of(club));
        given(s3Transferer.downloadOne(clubDetailInfoDto.getLogo())).willReturn(logoS3DownloadDto);
        given(s3Transferer.downloadAll(clubDetailInfoDto.getActivityImages())).willReturn(activityImgS3DownloadDtos);

        //when
        ResultActions actions = mockMvc.perform(
                get("/club/my")
        );

        //then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clubDetailInfoDto.getId()))
                .andExpect(jsonPath("$.campus").value(clubDetailInfoDto.getCampus().toString()))
                .andExpect(jsonPath("$.clubType").value(clubDetailInfoDto.getClubType().toString()))
                .andExpect(jsonPath("$.belongs").value(clubDetailInfoDto.getBelongs()))
                .andExpect(jsonPath("$.briefActivityDescription").value(clubDetailInfoDto.getBriefActivityDescription()))
                .andExpect(jsonPath("$.name").value(clubDetailInfoDto.getName()))
                .andExpect(jsonPath("$.headLine").value(clubDetailInfoDto.getHeadLine()))
                .andExpect(jsonPath("$.establishAt").value(clubDetailInfoDto.getEstablishAt()))
                .andExpect(jsonPath("$.roomLocation").value(clubDetailInfoDto.getRoomLocation()))
                .andExpect(jsonPath("$.memberAmount").value(clubDetailInfoDto.getMemberAmount()))
                .andExpect(jsonPath("$.regularMeetingTime").value(clubDetailInfoDto.getRegularMeetingTime()))
                .andExpect(jsonPath("$.mandatoryActivatePeriod").value(clubDetailInfoDto.getMandatoryActivatePeriod()))
                .andExpect(jsonPath("$.clubDescription").value(clubDetailInfoDto.getClubDescription()))
                .andExpect(jsonPath("$.activityDescription").value(clubDetailInfoDto.getActivityDescription()))
                .andExpect(jsonPath("$.webLink1").value(clubDetailInfoDto.getWebLink1()))
                .andExpect(jsonPath("$.webLink2").value(clubDetailInfoDto.getWebLink2()))
                .andExpect(jsonPath("$.presidentName").value(clubDetailInfoDto.getPresidentName()))
                .andExpect(jsonPath("$.presidentContact").value(clubDetailInfoDto.getPresidentContact()))
                .andExpect(jsonPath("$.logo.id").value(logoS3DownloadDto.getId()))
                .andExpect(jsonPath("$.logo.fileName").value(logoS3DownloadDto.getFileName()))
                .andExpect(jsonPath("$.logo.url").value(logoS3DownloadDto.getUrl()));
        clubDetailInfoDto.getRecruit().ifPresent(r -> {
            try {
                checkRecruitResponseJson(actions, r);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        for(int i = 0; i < testDataRepository.getActivityImgPerClub(); i++) {
            checkActivityImagesResponseJson(actions, i, activityImgS3DownloadDtos);
        }

        actions.andDo(
                document("club/get/detail/my",
                        responseFields(
                                fieldWithPath("id").type(WireFormat.FieldType.INT64).description("동아리 Id").attributes(example(clubDetailInfoDto.getId().toString())),
                                fieldWithPath("campus").type(WireFormat.FieldType.STRING).description("분류 - 캠퍼스").attributes(example(clubDetailInfoDto.getCampus().toString())),
                                fieldWithPath("clubType").type(WireFormat.FieldType.STRING).description("분류 - 동아리 종류").attributes(example(clubDetailInfoDto.getClubType().toString())),
                                fieldWithPath("belongs").type(WireFormat.FieldType.STRING).description("분류 - 동아리 분과").attributes(example(clubDetailInfoDto.getBelongs())),
                                fieldWithPath("briefActivityDescription").type(WireFormat.FieldType.STRING).description("분류 - 활동 설명").attributes(example(clubDetailInfoDto.getBriefActivityDescription())),
                                fieldWithPath("name").type(WireFormat.FieldType.STRING).description("동아리 이름").attributes(example(clubDetailInfoDto.getName())),
                                fieldWithPath("headLine").type(WireFormat.FieldType.STRING).description("한줄 소개").attributes(example(clubDetailInfoDto.getHeadLine())),
                                fieldWithPath("establishAt").type(WireFormat.FieldType.STRING).description("설립 연도").attributes(example(clubDetailInfoDto.getEstablishAt().toString())),
                                fieldWithPath("roomLocation").type(WireFormat.FieldType.STRING).description("동아리 방 위치").attributes(example(clubDetailInfoDto.getRoomLocation())),
                                fieldWithPath("memberAmount").type(WireFormat.FieldType.STRING).description("동아리 인원").attributes(example(clubDetailInfoDto.getMemberAmount().toString())),
                                fieldWithPath("regularMeetingTime").type(WireFormat.FieldType.STRING).description("정규 모임 시간").attributes(example(clubDetailInfoDto.getRegularMeetingTime())),
                                fieldWithPath("mandatoryActivatePeriod").type(WireFormat.FieldType.STRING).description("의무 활동 기간").attributes(example(clubDetailInfoDto.getMandatoryActivatePeriod())),
                                fieldWithPath("clubDescription").type(WireFormat.FieldType.STRING).description("자세한 동아리 설명").attributes(example(clubDetailInfoDto.getClubDescription())),
                                fieldWithPath("activityDescription").type(WireFormat.FieldType.STRING).description("자세한 활동 설명").attributes(example(clubDetailInfoDto.getActivityDescription())),
                                fieldWithPath("webLink1").type(WireFormat.FieldType.STRING).description("관련 사이트 주소 1").attributes(example(clubDetailInfoDto.getWebLink1())),
                                fieldWithPath("webLink2").type(WireFormat.FieldType.STRING).description("관련 사이트 주소 2").attributes(example(clubDetailInfoDto.getWebLink2())),
                                fieldWithPath("recruit.recruitStartAt").type(WireFormat.FieldType.STRING).description("모집 시작일 (* 상시 모집의 경우엔 NULL)").attributes(example("yyyy-MM-ddTHH:mm (null when not recruting)")),
                                fieldWithPath("recruit.recruitEndAt").type(WireFormat.FieldType.STRING).description("모집 종료일 (* 상시 모집의 경우엔 NULL)").attributes(example("yyyy-MM-ddTHH:mm (null when not recruting)")),
                                fieldWithPath("recruit.recruitQuota").type(WireFormat.FieldType.STRING).description("모집 인원").attributes(example("10 ~ 30명 - Can Any Format(null when not recruting)")),
                                fieldWithPath("recruit.recruitProcessDescription").type(WireFormat.FieldType.STRING).description("모집 절차 설명").attributes(example("Test Recruit Format(null when not recruting)")),
                                fieldWithPath("recruit.recruitContact").type(WireFormat.FieldType.STRING).description("모집 문의처").attributes(example("010-1234-1234 or recruit@asd.asd - Can Any Format(null when not recruting)")),
                                fieldWithPath("recruit.recruitWebLink").type(WireFormat.FieldType.STRING).description("모집 링크").attributes(example("form.goole.com - Can Any Format(null when not recruting)")),
                                fieldWithPath("presidentName").type(WireFormat.FieldType.STRING).description("회장 이름").attributes(example(clubDetailInfoDto.getPresidentName())),
                                fieldWithPath("presidentContact").type(WireFormat.FieldType.STRING).description("회장 연락처").attributes(example(clubDetailInfoDto.getPresidentContact())),
                                fieldWithPath("logo.id").type(WireFormat.FieldType.INT64).description("로고 ID").attributes(example(logoS3DownloadDto.getId().toString())),
                                fieldWithPath("logo.fileName").type(WireFormat.FieldType.STRING).description("로고 파일명").attributes(example(logoS3DownloadDto.getFileName())),
                                fieldWithPath("logo.url").type(WireFormat.FieldType.STRING).description("로고 리소스 주소").attributes(example("https://s3.ap-northeast-2.amazonaws.com/skklub.test/024f3d7b-0ae0-4011-8f3f-23637d10f3d4.jpg")),
                                fieldWithPath("activityImages[].id").type(WireFormat.FieldType.STRING).description("활동 사진 ID").attributes(example(activityImgS3DownloadDtos.get(0).getId().toString())),
                                fieldWithPath("activityImages[].fileName").type(WireFormat.FieldType.STRING).description("활동 사진 파일명").attributes(example(activityImgS3DownloadDtos.get(0).getFileName())),
                                fieldWithPath("activityImages.[]url").type(WireFormat.FieldType.STRING).description("활동 사진 리소스 주소") .attributes(example("https://s3.ap-northeast-2.amazonaws.com/skklub.test/024f3d7b-0ae0-4011-8f3f-23637d10f3d4.jpg"))
                        )
                )
        );
    }
    @Test
    @WithMockUser
    public void getMyClubByLoginUser_LoginAsAdmin_AdminCannotHaveClubException() throws Exception{
        //given
        String username = "user";
        User user = new User(username, null, Role.ROLE_ADMIN_SEOUL_CENTRAL, null, null);
        given(userRepository.findByUsername(username)).willReturn(user);

        //when
        MvcResult adminLoginResult = mockMvc.perform(
                        get("/club/my")
                ).andExpect(status().isBadRequest())
                .andReturn();

        //then
        assertThat(adminLoginResult.getResolvedException()).isExactlyInstanceOf(AdminCannotHaveClubException.class);

    }

    @Test
    public void getClubByName_Default_Success() throws Exception{
        //given
        long clubId = 0L;
        Club club = testDataRepository.getClubs().get((int) clubId);
        Field id = club.getClass().getDeclaredField("id");
        id.setAccessible(true);
        id.set(club, clubId);
        ClubDetailInfoDto clubDetailInfoDto = testDataRepository.getClubDetailInfoDtos().get((int) clubId);
        String clubName = clubDetailInfoDto.getName();
        S3DownloadDto logoS3DownloadDto = testDataRepository.getLogoS3DownloadDto((int) clubId);
        List<S3DownloadDto> activityImgS3DownloadDtos = testDataRepository.getActivityImgS3DownloadDtos((int) clubId);
        given(clubRepository.findDetailClubByName(clubName)).willReturn(Optional.of(club));
        given(s3Transferer.downloadOne(clubDetailInfoDto.getLogo())).willReturn(logoS3DownloadDto);
        given(s3Transferer.downloadAll(clubDetailInfoDto.getActivityImages())).willReturn(activityImgS3DownloadDtos);

        //when
        ResultActions actions = mockMvc.perform(
                get("/club/search")
                        .with(csrf())
                        .queryParam("name", clubName)
        );

        //then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clubDetailInfoDto.getId()))
                .andExpect(jsonPath("$.campus").value(clubDetailInfoDto.getCampus().toString()))
                .andExpect(jsonPath("$.clubType").value(clubDetailInfoDto.getClubType().toString()))
                .andExpect(jsonPath("$.belongs").value(clubDetailInfoDto.getBelongs()))
                .andExpect(jsonPath("$.briefActivityDescription").value(clubDetailInfoDto.getBriefActivityDescription()))
                .andExpect(jsonPath("$.name").value(clubDetailInfoDto.getName()))
                .andExpect(jsonPath("$.headLine").value(clubDetailInfoDto.getHeadLine()))
                .andExpect(jsonPath("$.establishAt").value(clubDetailInfoDto.getEstablishAt()))
                .andExpect(jsonPath("$.roomLocation").value(clubDetailInfoDto.getRoomLocation()))
                .andExpect(jsonPath("$.memberAmount").value(clubDetailInfoDto.getMemberAmount()))
                .andExpect(jsonPath("$.regularMeetingTime").value(clubDetailInfoDto.getRegularMeetingTime()))
                .andExpect(jsonPath("$.mandatoryActivatePeriod").value(clubDetailInfoDto.getMandatoryActivatePeriod()))
                .andExpect(jsonPath("$.clubDescription").value(clubDetailInfoDto.getClubDescription()))
                .andExpect(jsonPath("$.activityDescription").value(clubDetailInfoDto.getActivityDescription()))
                .andExpect(jsonPath("$.webLink1").value(clubDetailInfoDto.getWebLink1()))
                .andExpect(jsonPath("$.webLink2").value(clubDetailInfoDto.getWebLink2()))
                .andExpect(jsonPath("$.presidentName").value(clubDetailInfoDto.getPresidentName()))
                .andExpect(jsonPath("$.presidentContact").value(clubDetailInfoDto.getPresidentContact()))
                .andExpect(jsonPath("$.logo.id").value(logoS3DownloadDto.getId()))
                .andExpect(jsonPath("$.logo.fileName").value(logoS3DownloadDto.getFileName()))
                .andExpect(jsonPath("$.logo.url").value(logoS3DownloadDto.getUrl()));
        clubDetailInfoDto.getRecruit().ifPresent(r -> {
            try {
                checkRecruitResponseJson(actions, r);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        for(int i = 0; i < testDataRepository.getActivityImgPerClub(); i++) {
            checkActivityImagesResponseJson(actions, i, activityImgS3DownloadDtos);
        }
        actions.andDo(
                document("club/get/detail/search",
                        queryParameters(
                                parameterWithName("name").attributes(example(clubName)).description("동아리 이름(정확히 일치 시에만)")
                        ),
                        responseFields(
                                fieldWithPath("id").type(WireFormat.FieldType.INT64).description("동아리 Id").attributes(example(clubDetailInfoDto.getId().toString())),
                                fieldWithPath("campus").type(WireFormat.FieldType.STRING).description("분류 - 캠퍼스").attributes(example(clubDetailInfoDto.getCampus().toString())),
                                fieldWithPath("clubType").type(WireFormat.FieldType.STRING).description("분류 - 동아리 종류").attributes(example(clubDetailInfoDto.getClubType().toString())),
                                fieldWithPath("belongs").type(WireFormat.FieldType.STRING).description("분류 - 동아리 분과").attributes(example(clubDetailInfoDto.getBelongs())),
                                fieldWithPath("briefActivityDescription").type(WireFormat.FieldType.STRING).description("분류 - 활동 설명").attributes(example(clubDetailInfoDto.getBriefActivityDescription())),
                                fieldWithPath("name").type(WireFormat.FieldType.STRING).description("동아리 이름").attributes(example(clubDetailInfoDto.getName())),
                                fieldWithPath("headLine").type(WireFormat.FieldType.STRING).description("한줄 소개").attributes(example(clubDetailInfoDto.getHeadLine())),
                                fieldWithPath("establishAt").type(WireFormat.FieldType.STRING).description("설립 연도").attributes(example(clubDetailInfoDto.getEstablishAt().toString())),
                                fieldWithPath("roomLocation").type(WireFormat.FieldType.STRING).description("동아리 방 위치").attributes(example(clubDetailInfoDto.getRoomLocation())),
                                fieldWithPath("memberAmount").type(WireFormat.FieldType.STRING).description("동아리 인원").attributes(example(clubDetailInfoDto.getMemberAmount().toString())),
                                fieldWithPath("regularMeetingTime").type(WireFormat.FieldType.STRING).description("정규 모임 시간").attributes(example(clubDetailInfoDto.getRegularMeetingTime())),
                                fieldWithPath("mandatoryActivatePeriod").type(WireFormat.FieldType.STRING).description("의무 활동 기간").attributes(example(clubDetailInfoDto.getMandatoryActivatePeriod())),
                                fieldWithPath("clubDescription").type(WireFormat.FieldType.STRING).description("자세한 동아리 설명").attributes(example(clubDetailInfoDto.getClubDescription())),
                                fieldWithPath("activityDescription").type(WireFormat.FieldType.STRING).description("자세한 활동 설명").attributes(example(clubDetailInfoDto.getActivityDescription())),
                                fieldWithPath("webLink1").type(WireFormat.FieldType.STRING).description("관련 사이트 주소 1").attributes(example(clubDetailInfoDto.getWebLink1())),
                                fieldWithPath("webLink2").type(WireFormat.FieldType.STRING).description("관련 사이트 주소 2").attributes(example(clubDetailInfoDto.getWebLink2())),
                                fieldWithPath("recruit.recruitStartAt").type(WireFormat.FieldType.STRING).description("모집 시작일 (* 상시 모집의 경우엔 NULL)").attributes(example("yyyy-MM-ddTHH:mm (null when not recruting)")),
                                fieldWithPath("recruit.recruitEndAt").type(WireFormat.FieldType.STRING).description("모집 종료일 (* 상시 모집의 경우엔 NULL)").attributes(example("yyyy-MM-ddTHH:mm (null when not recruting)")),
                                fieldWithPath("recruit.recruitQuota").type(WireFormat.FieldType.STRING).description("모집 인원").attributes(example("10 ~ 30명 - Can Any Format(null when not recruting)")),
                                fieldWithPath("recruit.recruitProcessDescription").type(WireFormat.FieldType.STRING).description("모집 절차 설명").attributes(example("Test Recruit Format(null when not recruting)")),
                                fieldWithPath("recruit.recruitContact").type(WireFormat.FieldType.STRING).description("모집 문의처").attributes(example("010-1234-1234 or recruit@asd.asd - Can Any Format(null when not recruting)")),
                                fieldWithPath("recruit.recruitWebLink").type(WireFormat.FieldType.STRING).description("모집 링크").attributes(example("form.goole.com - Can Any Format(null when not recruting)")),
                                fieldWithPath("presidentName").type(WireFormat.FieldType.STRING).description("회장 이름").attributes(example(clubDetailInfoDto.getPresidentName())),
                                fieldWithPath("presidentContact").type(WireFormat.FieldType.STRING).description("회장 연락처").attributes(example(clubDetailInfoDto.getPresidentContact())),
                                fieldWithPath("logo.id").type(WireFormat.FieldType.INT64).description("로고 ID").attributes(example(logoS3DownloadDto.getId().toString())),
                                fieldWithPath("logo.fileName").type(WireFormat.FieldType.STRING).description("로고 파일명").attributes(example(logoS3DownloadDto.getFileName())),
                                fieldWithPath("logo.url").type(WireFormat.FieldType.STRING).description("로고 파일 리소스 주소").attributes(example("https://s3.ap-northeast-2.amazonaws.com/skklub.test/ASDASD-QEQWE.jpg")),
                                fieldWithPath("activityImages[].id").type(WireFormat.FieldType.STRING).description("활동 사진 ID").attributes(example(activityImgS3DownloadDtos.get(0).getId().toString())),
                                fieldWithPath("activityImages[].fileName").type(WireFormat.FieldType.STRING).description("활동 사진 파일명").attributes(example(activityImgS3DownloadDtos.get(0).getFileName())),
                                fieldWithPath("activityImages[].url").type(WireFormat.FieldType.STRING).description("활동 사진 리소스 주소").attributes(example("https://s3.ap-northeast-2.amazonaws.com/skklub.test/ASDASD-QEQWE.jpg"))
                        )
                )
        );
    }

    @Test
    public void getClubByName_NoMatchClubName_ClubNameMisMatchException() throws Exception{
        //given
        String clubName = "이 이름은 절대로 없을꺼야 ㅋㅋ";
        given(clubRepository.findDetailClubByName(clubName)).willReturn(Optional.empty());

        //when
        MvcResult badNameResult = mockMvc.perform(
                get("/club/search")
                        .with(csrf())
                        .queryParam("name", clubName)
        ).andExpect(status().isBadRequest()).andReturn();

        //then
        assertThat(badNameResult.getResolvedException()).isExactlyInstanceOf(ClubNameMisMatchException.class);
     }

    @Test
    public void getClubPrevByCategories_FullCategory_Success() throws Exception{
        //given
        Campus campus = Campus.명륜;
        ClubType clubType = ClubType.중앙동아리;
        String belongs = "취미교양";
        int clubPerPage = 5;
        PageRequest request = PageRequest.of(0, clubPerPage, Sort.by("id").ascending().and(Sort.by("name").ascending()));
        List<Club> clubs = testDataRepository.getClubs();
        setClubIds(clubs);
        Page<Club> clubPage = new PageImpl<>(clubs, request, clubs.size());

        given(clubService.getClubPrevsByCategories(campus, clubType, belongs, request)).willReturn(clubPage);
        clubPage.stream()
                .forEach(page -> given(s3Transferer.downloadOne(new FileNames(page.getLogo()))).willReturn(testDataRepository.getLogoS3DownloadDto(page.getId().intValue())));
        //when
        ResultActions actions = mockMvc.perform(
                get("/club/prev")
                        .with(csrf())
                        .queryParam("campus", campus.toString())
                        .queryParam("clubType", clubType.toString())
                        .queryParam("belongs", belongs)
                        .queryParam("size", String.valueOf(clubPerPage))
                        .queryParam("page", "0")
                        .queryParam("sort", "id,ASC")
        );

        //then
        actions = actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(clubPerPage))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.pageable.sort.sorted").value("true"));
        for(int i = 0; i < clubPerPage; i++) {
            actions = buildPageableResponseContentChecker(actions, i);
        }

        List<FieldDescriptor> pageableResponseFields = new ArrayList<>();
        pageableResponseFields.add(fieldWithPath("content[].id").type(WireFormat.FieldType.INT64).description("동아리 아이디").attributes(example(clubs.get(0).getId().toString())));
        pageableResponseFields.add(fieldWithPath("content[].name").type(WireFormat.FieldType.STRING).description("동아리 이름").attributes(example(clubs.get(0).getName())));
        pageableResponseFields.add(fieldWithPath("content[].campus").type(WireFormat.FieldType.STRING).description("분류 - 캠퍼스").attributes(example(LINK_CAMPUS_TYPE)));
        pageableResponseFields.add(fieldWithPath("content[].clubType").type(WireFormat.FieldType.STRING).description("분류 - 동아리 종류").attributes(example(LINK_CLUB_TYPE)));
        pageableResponseFields.add(fieldWithPath("content[].belongs").type(WireFormat.FieldType.STRING).description("분류 - 분과").attributes(example(LINK_BELONGS_TYPE)));
        pageableResponseFields.add(fieldWithPath("content[].briefActivityDescription").type(WireFormat.FieldType.STRING).description("분류 - 활동 설명").attributes(example(clubs.get(0).getBriefActivityDescription())));
        pageableResponseFields.add(fieldWithPath("content[].logo.id").type(WireFormat.FieldType.INT64).description("로고 아이디").attributes(example(testDataRepository.getLogoS3DownloadDto(clubs.get(0).getId().intValue()).getId().toString())));
        pageableResponseFields.add(fieldWithPath("content[].logo.fileName").type(WireFormat.FieldType.STRING).description("로고 원본 파일명").attributes(example(testDataRepository.getLogoS3DownloadDto(clubs.get(0).getId().intValue()).getFileName())));
        pageableResponseFields.add(fieldWithPath("content[].logo.url").type(WireFormat.FieldType.STRING).description("로고 리소스 주소").attributes(example("https://s3.ap-northeast-2.amazonaws.com/skklub.test/024f3d7b-0ae0-4011-8f3f-23637d10f3d4.jpg")));
        addPageableResponseFields(pageableResponseFields);

        actions.andDo(
                document("club/get/prevs/category",
                        queryParameters(
                                parameterWithName("campus").description("분류 - 캠퍼스").attributes(example(LINK_CAMPUS_TYPE)),
                                parameterWithName("clubType").description("분류 - 동아리 종류").attributes(example(LINK_CLUB_TYPE_NULL)).optional(),
                                parameterWithName("belongs").description("분류 - 동아리 분과").attributes(example(LINK_BELONGS_TYPE_NULL)).optional(),
                                parameterWithName("size").optional().description("페이지 정보 - 한 페이지 크기").attributes(example("Default : 20")),
                                parameterWithName("page").optional().description("페이지 정보 - 요청 페이지 번호(시작 0)").attributes(example("Default : 0")),
                                parameterWithName("sort").optional().description("페이지 정보 - 정렬").attributes(example(RestDocsUtils.LINK_SORT_CLUB))
                        ),
                        responseFields(
                                pageableResponseFields
                        )
                )
        );
    }

    private void setClubIds(List<Club> clubs) throws NoSuchFieldException, IllegalAccessException {
        for(int i = 0; i < clubs.size(); i++){
            Field id = clubs.get(i).getClass().getDeclaredField("id");
            id.setAccessible(true);
            id.set(clubs.get(i), (long)i);
        }
    }

    @Test
    public void getClubPrevByCategories_NoSort_SortByNameASC() throws Exception{
        //given
        Campus campus = Campus.명륜;
        ClubType clubType = ClubType.준중앙동아리;
        String belongs = "전체";
        int clubPerPage = 5;
        PageRequest request = PageRequest.of(0, clubPerPage, Sort.by("name").ascending());
        List<Club> clubs = testDataRepository.getClubs();
        setClubIds(clubs);
        Page<Club> clubPage = new PageImpl<>(clubs, request, clubs.size());

        given(clubService.getClubPrevsByCategories(campus, clubType, belongs, request)).willReturn(clubPage);
        clubPage.stream()
                .forEach(pages -> given(s3Transferer.downloadOne(new FileNames(pages.getLogo()))).willReturn(testDataRepository.getLogoS3DownloadDto(pages.getId().intValue())));
        //when
        ResultActions actions = mockMvc.perform(
                get("/club/prev")
                        .with(csrf())
                        .queryParam("campus", campus.toString())
                        .queryParam("clubType", clubType.toString())
                        .queryParam("size", String.valueOf(clubPerPage))
                        .queryParam("page", "0")
        );

        //then
        actions = actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(clubPerPage))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.pageable.sort.sorted").value("true"));
        for(int i = 0; i < clubPerPage; i++) {
            actions = buildPageableResponseContentChecker(actions, i);
        }
    }
    @Test
    public void getClubPrevByCategories_NoBelongs_Success() throws Exception{
        //given
        Campus campus = Campus.명륜;
        ClubType clubType = ClubType.준중앙동아리;
        String belongs = "전체";
        int clubPerPage = 5;
        PageRequest request = PageRequest.of(0, clubPerPage, Sort.by("id").ascending().and(Sort.by("name").ascending()));
        List<Club> clubs = testDataRepository.getClubs();
        setClubIds(clubs);
        Page<Club> clubPage = new PageImpl<>(clubs, request, clubs.size());

        given(clubService.getClubPrevsByCategories(campus, clubType, belongs, request)).willReturn(clubPage);
        clubPage.stream()
                .forEach(pages -> given(s3Transferer.downloadOne(new FileNames(pages.getLogo()))).willReturn(testDataRepository.getLogoS3DownloadDto(pages.getId().intValue())));
        //when
        ResultActions actions = mockMvc.perform(
                get("/club/prev")
                        .with(csrf())
                        .queryParam("campus", campus.toString())
                        .queryParam("clubType", clubType.toString())
                        .queryParam("size", String.valueOf(clubPerPage))
                        .queryParam("page", "0")
                        .queryParam("sort", "id,ASC")
        );

        //then
        actions = actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(clubPerPage))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.pageable.sort.sorted").value("true"));
        for(int i = 0; i < clubPerPage; i++) {
            actions = buildPageableResponseContentChecker(actions, i);
        }
    }

    @Test
    public void getClubPrevByCategories_NoClubTypeAndAnyBelongs_Success() throws Exception{
        //given
        Campus campus = Campus.명륜;
        ClubType clubType = ClubType.전체;
        String belongs = "AnyBelongsString";
        int clubPerPage = 5;
        PageRequest request = PageRequest.of(0, clubPerPage, Sort.by("id").ascending().and(Sort.by("name").ascending()));
        List<Club> clubs = testDataRepository.getClubs();
        setClubIds(clubs);
        Page<Club> clubPage = new PageImpl<>(clubs, request, clubs.size());

        given(clubService.getClubPrevsByCategories(campus, clubType, belongs, request)).willReturn(clubPage);
        clubPage.stream()
                .forEach(page -> given(s3Transferer.downloadOne(new FileNames(page.getLogo()))).willReturn(testDataRepository.getLogoS3DownloadDto(page.getId().intValue())));

        //when
        ResultActions actions = mockMvc.perform(
                get("/club/prev")
                        .with(csrf())
                        .queryParam("campus", campus.toString())
                        .queryParam("belongs", belongs)
                        .queryParam("size", String.valueOf(clubPerPage))
                        .queryParam("page", "0")
                        .queryParam("sort", "id,ASC")
        );

        //then
        actions = actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(clubPerPage))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.pageable.sort.sorted").value("true"));
        for(int i = 0; i < clubPerPage; i++) {
            actions = buildPageableResponseContentChecker(actions, i);
        }
    }

    @Test
    public void getClubPrevByCategories_NoClubTypeAndNoBelongs_Success() throws Exception{
        //given
        Campus campus = Campus.명륜;
        ClubType clubType = ClubType.전체;
        String belongs = "전체";
        int clubPerPage = 5;
        PageRequest request = PageRequest.of(0, clubPerPage, Sort.by("id").ascending().and(Sort.by("name").ascending()));
        List<Club> clubs = testDataRepository.getClubs();
        setClubIds(clubs);
        Page<Club> clubPage = new PageImpl<>(clubs, request, clubs.size());

        given(clubService.getClubPrevsByCategories(campus, clubType, belongs, request)).willReturn(clubPage);
        clubPage.stream()
                .forEach(page -> given(s3Transferer.downloadOne(new FileNames(page.getLogo()))).willReturn(testDataRepository.getLogoS3DownloadDto(page.getId().intValue())));

        //when
        ResultActions actions = mockMvc.perform(
                get("/club/prev")
                        .with(csrf())
                        .queryParam("campus", campus.toString())
                        .queryParam("clubType", "")
                        .queryParam("size", String.valueOf(clubPerPage))
                        .queryParam("page", "0")
                        .queryParam("sort", "id,ASC")
        );

        //then
        actions = actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(clubPerPage))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.pageable.sort.sorted").value("true"));
        for(int i = 0; i < clubPerPage; i++) {
            actions = buildPageableResponseContentChecker(actions, i);
        }
    }

    @Test
    public void getClubPrevByCategories_IllegalBelongsType_InvalidBelongsException() throws Exception{
        //given
        Campus campus = Campus.명륜;
        ClubType clubType = ClubType.중앙동아리;
        String belongs = "IllegalCategory";

        //when
        MvcResult badBelongsResult = mockMvc.perform(
                get("/club/prev")
                        .queryParam("campus", campus.toString())
                        .queryParam("clubType", clubType.toString())
                        .queryParam("belongs", belongs)
                        .queryParam("size", String.valueOf(5))
                        .queryParam("page", "0")
                        .queryParam("sort", "campus,ASC")
        ).andExpect(status().isBadRequest()).andReturn();

        //then
        assertThat(badBelongsResult.getResolvedException()).isExactlyInstanceOf(InvalidBelongsException.class);
     }

    @Test
    public void getClubPrevByKeyword_Default_Success() throws Exception{
        //given
        String keyword = "SKKU";
        int clubPerPage = 5;
        PageRequest request = PageRequest.of(0, clubPerPage, Sort.by("campus").ascending().and(Sort.by("name").ascending()));
        List<Club> clubs = testDataRepository.getClubs();
        setClubIds(clubs);
        Page<Club> clubPage = new PageImpl<>(clubs, request, clubs.size());
        given(clubRepository.findClubByNameContaining(keyword, request)).willReturn(clubPage);
        clubPage.stream()
                .forEach(page -> given(s3Transferer.downloadOne(new FileNames(page.getLogo()))).willReturn(testDataRepository.getLogoS3DownloadDto((int) (long) page.getId())));

        //when
        ResultActions actions = mockMvc.perform(
                get("/club/search/prevs")
                        .with(csrf())
                        .queryParam("keyword", keyword)
                        .queryParam("size", String.valueOf(clubPerPage))
                        .queryParam("page", "0")
                        .queryParam("sort", "campus,ASC")
        );

        //then
        actions = actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(clubPerPage))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.pageable.sort.sorted").value("true"));
        for(int i = 0; i < clubPerPage; i++) {
            actions = buildPageableResponseContentChecker(actions, i);
        }
        List<FieldDescriptor> pageableResponseFields = new ArrayList<>();
        pageableResponseFields.add(fieldWithPath("content[].id").type(WireFormat.FieldType.INT64).description("동아리 아이디").attributes(example(clubs.get(0).getId().toString())));
        pageableResponseFields.add(fieldWithPath("content[].name").type(WireFormat.FieldType.STRING).description("동아리 이름").attributes(example(clubs.get(0).getName())));
        pageableResponseFields.add(fieldWithPath("content[].campus").type(WireFormat.FieldType.STRING).description("분류 - 캠퍼스").attributes(example(LINK_CAMPUS_TYPE)));
        pageableResponseFields.add(fieldWithPath("content[].clubType").type(WireFormat.FieldType.STRING).description("분류 - 동아리 종류").attributes(example(LINK_CLUB_TYPE)));
        pageableResponseFields.add(fieldWithPath("content[].belongs").type(WireFormat.FieldType.STRING).description("분류 - 분과").attributes(example(LINK_BELONGS_TYPE)));
        pageableResponseFields.add(fieldWithPath("content[].briefActivityDescription").type(WireFormat.FieldType.STRING).description("분류 - 활동 설명").attributes(example(clubs.get(0).getBriefActivityDescription())));
        pageableResponseFields.add(fieldWithPath("content[].logo.id").type(WireFormat.FieldType.INT64).description("로고 아이디").attributes(example(testDataRepository.getLogoS3DownloadDto(clubs.get(0).getId().intValue()).getId().toString())));
        pageableResponseFields.add(fieldWithPath("content[].logo.fileName").type(WireFormat.FieldType.STRING).description("로고 원본 파일명").attributes(example(testDataRepository.getLogoS3DownloadDto(clubs.get(0).getId().intValue()).getFileName())));
        pageableResponseFields.add(fieldWithPath("content[].logo.url").type(WireFormat.FieldType.STRING).description("로고 리소스 주소").attributes(example("https://s3.ap-northeast-2.amazonaws.com/skklub.test/024f3d7b-0ae0-4011-8f3f-23637d10f3d4.jpg")));
        addPageableResponseFields(pageableResponseFields);

        actions.andDo(
                document("club/get/prevs/search",
                        queryParameters(
                                parameterWithName("keyword").description("동아리 이름 검색 키워드").attributes(example("%Keyword%")),
                                parameterWithName("size").optional().description("페이지 정보 - 한 페이지 크기").attributes(example("Default : 20")),
                                parameterWithName("page").optional().description("페이지 정보 - 요청 페이지 번호(시작 0)").attributes(example("Default : 0")),
                                parameterWithName("sort").optional().description("페이지 정보 - 정렬").attributes(example(RestDocsUtils.LINK_SORT_CLUB))
                        ),
                        responseFields(
                                pageableResponseFields
                        )
                )
        );
     }

    @Test
    public void getClubPrevByKeyword_NoSort_SortByNameASC() throws Exception{
        //given
        String keyword = "SKKU";
        int clubPerPage = 5;
        PageRequest request = PageRequest.of(0, clubPerPage, Sort.by("name").ascending());
        List<Club> clubs = testDataRepository.getClubs();
        setClubIds(clubs);
        Page<Club> clubPage = new PageImpl<>(clubs, request, clubs.size());
        given(clubRepository.findClubByNameContaining(keyword, request)).willReturn(clubPage);
        clubPage.stream()
                .forEach(page -> given(s3Transferer.downloadOne(new FileNames(page.getLogo()))).willReturn(testDataRepository.getLogoS3DownloadDto((int) (long) page.getId())));

        //when
        ResultActions actions = mockMvc.perform(
                get("/club/search/prevs")
                        .with(csrf())
                        .queryParam("keyword", keyword)
                        .queryParam("size", String.valueOf(clubPerPage))
                        .queryParam("page", "0")
        );

        //then
        actions = actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(clubPerPage))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.pageable.sort.sorted").value("true"));
        for(int i = 0; i < clubPerPage; i++) {
            actions = buildPageableResponseContentChecker(actions, i);
        }
     }

    @Test
    public void getClubPrevByKeyword_BlankKeyword_ReturnEmptyPage() throws Exception{
        //given
        String keyword = "";
        int clubPerPage = 5;
        List<Club> clubs = testDataRepository.getClubs();
        setClubIds(clubs);

        //when
        ResultActions actions = mockMvc.perform(
                get("/club/search/prevs")
                        .with(csrf())
                        .queryParam("keyword", keyword)
                        .queryParam("size", String.valueOf(clubPerPage))
                        .queryParam("page", "0")
                        .queryParam("sort", "campus,ASC")
        );

        //then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(0))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(0));
     }

    @Test
    public void getRandomClubNameAndIdByCategories_Default_Success() throws Exception {
        //given
        Campus campus = Campus.명륜;
        ClubType clubType = ClubType.중앙동아리;
        String belongs = "취미교양";
        List<Club> clubs = testDataRepository.getClubs().subList(0, 3);
        setClubIds(clubs);
        given(clubService.getRandomClubsByCategories(campus, clubType, belongs)).willReturn(clubs);

        //when
        ResultActions actions = mockMvc.perform(
                get("/club/random")
                        .with(csrf())
                        .queryParam("campus", campus.toString())
                        .queryParam("clubType", clubType.toString())
                        .queryParam("belongs", belongs)
        );

        //then
        actions.andExpect(status().isOk());
        for (int i = 0; i < clubs.size(); i++) {
            actions.andExpect(jsonPath("$[" + i + "].id").value(clubs.get(i).getId()))
                    .andExpect(jsonPath("$[" + i + "].name").value(clubs.get(i).getName()))
                    .andExpect(jsonPath("$[" + i + "].campus").value(clubs.get(i).getCampus().toString()));
        }
        actions.andDo(
                document("club/get/random",
                        queryParameters(
                                parameterWithName("campus").description("분류 - 캠퍼스").attributes(example(LINK_CAMPUS_TYPE)),
                                parameterWithName("clubType").description("분류 - 동아리 종류").attributes(example(LINK_CLUB_TYPE_NULL)).optional(),
                                parameterWithName("belongs").description("분류 - 동아리 분과").attributes(example(LINK_BELONGS_TYPE_NULL)).optional()
                        ),
                        responseFields(
                                fieldWithPath("[]id").type(WireFormat.FieldType.INT64).description("동아리 Id").attributes(example(clubs.get(0).getId().toString())),
                                fieldWithPath("[]name").type(WireFormat.FieldType.STRING).description("동아리 이름").attributes(example(clubs.get(0).getName())),
                                fieldWithPath("[]campus").type(WireFormat.FieldType.STRING).description("캠퍼스 종류").attributes(example(LINK_CAMPUS_TYPE))
                        )
                )
        );
    }

    @Test
    public void getRandomClubNameAndIdByCategories_IllegalBelongsType_InvalidBelongsException() throws Exception{
        //given
        Campus campus = Campus.명륜;
        ClubType clubType = ClubType.준중앙동아리;
        String belongs = "IllegalCategory";

        //when
        MvcResult badBelongsResult = mockMvc.perform(
                get("/club/random")
                        .queryParam("campus", campus.toString())
                        .queryParam("clubType", clubType.toString())
                        .queryParam("belongs", belongs)
                        .queryParam("size", String.valueOf(5))
                        .queryParam("page", "0")
                        .queryParam("sort", "name,ASC")
        ).andExpect(status().isBadRequest()).andReturn();

        //then
        assertThat(badBelongsResult.getResolvedException()).isExactlyInstanceOf(InvalidBelongsException.class);
    }

    private void checkActivityImagesResponseJson(ResultActions actions, int activityImgIndex, List<S3DownloadDto> activityImgS3DownloadDtos) throws Exception {
        actions.andExpect(jsonPath("$.activityImages[" + activityImgIndex + "].id").value(activityImgS3DownloadDtos.get(activityImgIndex).getId()))
                .andExpect(jsonPath("$.activityImages[" + activityImgIndex + "].fileName").value(activityImgS3DownloadDtos.get(activityImgIndex).getFileName()))
                .andExpect(jsonPath("$.activityImages[" + activityImgIndex + "].url").value(activityImgS3DownloadDtos.get(activityImgIndex).getUrl()));

    }
    private void checkRecruitResponseJson(ResultActions actions, RecruitDto recruitDto) throws Exception {
        actions.andExpect(jsonPath("$.recruit.recruitStartAt").value(recruitDto.getRecruitStartAt().toString()))
                .andExpect(jsonPath("$.recruit.recruitEndAt").value(recruitDto.getRecruitEndAt().toString()))
                .andExpect(jsonPath("$.recruit.recruitQuota").value(recruitDto.getRecruitQuota()))
                .andExpect(jsonPath("$.recruit.recruitProcessDescription").value(recruitDto.getRecruitProcessDescription()))
                .andExpect(jsonPath("$.recruit.recruitContact").value(recruitDto.getRecruitContact()))
                .andExpect(jsonPath("$.recruit.recruitWebLink").value(recruitDto.getRecruitWebLink()));
    }
    private ResultActions buildPageableResponseContentChecker(ResultActions actions, int index) throws Exception {
        Club club = testDataRepository.getClubs().get(index);
        S3DownloadDto s3Dto = testDataRepository.getLogoS3DownloadDto(index);
        return actions
                .andExpect(jsonPath("$.content[" + index + "].id").value(index))
                .andExpect(jsonPath("$.content[" + index + "].name").value(club.getName()))
                .andExpect(jsonPath("$.content[" + index + "].campus").value(club.getCampus().toString()))
                .andExpect(jsonPath("$.content[" + index + "].clubType").value(club.getClubType().toString()))
                .andExpect(jsonPath("$.content[" + index + "].belongs").value(club.getBelongs()))
                .andExpect(jsonPath("$.content[" + index + "].briefActivityDescription").value(club.getBriefActivityDescription()))
                .andExpect(jsonPath("$.content[" + index + "].logo.id").value(s3Dto.getId()))
                .andExpect(jsonPath("$.content[" + index + "].logo.fileName").value(s3Dto.getFileName()))
                .andExpect(jsonPath("$.content[" + index + "].logo.url").value(s3Dto.getUrl()));
    }

}