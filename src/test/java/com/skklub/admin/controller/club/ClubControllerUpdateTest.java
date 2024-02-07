package com.skklub.admin.controller.club;


import akka.protobuf.WireFormat;
import com.skklub.admin.TestDataRepository;
import com.skklub.admin.controller.AuthValidator;
import com.skklub.admin.controller.ClubController;
import com.skklub.admin.controller.RestDocsUtils;
import com.skklub.admin.controller.S3Transferer;
import com.skklub.admin.domain.Club;
import com.skklub.admin.domain.Logo;
import com.skklub.admin.domain.enums.ClubType;
import com.skklub.admin.exception.deprecated.error.exception.CannotDownGradeClubException;
import com.skklub.admin.exception.deprecated.error.exception.CannotUpGradeClubException;
import com.skklub.admin.exception.deprecated.error.exception.ClubIdMisMatchException;
import com.skklub.admin.repository.ClubRepository;
import com.skklub.admin.repository.UserRepository;
import com.skklub.admin.service.ClubService;
import com.skklub.admin.service.dto.FileNames;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Optional;

import static com.skklub.admin.controller.RestDocsUtils.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.multipart;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
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
class ClubControllerUpdateTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ClubService clubService;
    @MockBean
    private S3Transferer s3Transferer;
    @MockBean
    private ClubRepository clubRepository;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private AuthValidator authValidator;
    @InjectMocks
    private TestDataRepository testDataRepository;

    private MockMultipartFile mockLogo;

    @BeforeEach
    public void beforeEach() throws IOException {
        mockLogo = new MockMultipartFile(
                "logo",
                "test.png",
                ContentType.MULTIPART_FORM_DATA.toString(),
                new FileInputStream("src/main/resources/2020-12-25 (5).png")
        );
        doNothing().when(authValidator).validateUpdatingClub(anyLong());
        doNothing().when(authValidator).validateUpdatingNotice(anyLong());
        doNothing().when(authValidator).validateUpdatingUser(anyLong());
        doNothing().when(authValidator).validatePendingRequestAuthority(anyLong());
    }

    @Test
    public void updateClub_Default_Success() throws Exception {
        //given
        Long clubId = 0L;
        Long changeToId = 1L;
        Club changeTo = testDataRepository.getClubs().get(changeToId.intValue());
        doNothing().when(authValidator).validateUpdatingClub(clubId);
        given(clubService.updateClub(eq(clubId), any(Club.class))).willReturn(Optional.ofNullable(changeTo.getName()));

        //when
        ResultActions actions = mockMvc.perform(
                patch("/club/{clubId}", clubId)
                        .with(csrf())
                        .queryParam("clubName", changeTo.getName())
                        .queryParam("briefActivityDescription", changeTo.getBriefActivityDescription())
                        .queryParam("activityDescription", changeTo.getActivityDescription())
                        .queryParam("clubDescription", changeTo.getClubDescription())
                        .queryParam("establishDate", changeTo.getEstablishAt().toString())
                        .queryParam("headLine", changeTo.getHeadLine())
                        .queryParam("mandatoryActivatePeriod", changeTo.getMandatoryActivatePeriod())
                        .queryParam("memberAmount", changeTo.getMemberAmount().toString())
                        .queryParam("regularMeetingTime", changeTo.getRegularMeetingTime())
                        .queryParam("roomLocation", changeTo.getRoomLocation())
                        .queryParam("webLink1", changeTo.getWebLink1())
                        .queryParam("webLink2", changeTo.getWebLink2())
        );

        //then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clubId))
                .andExpect(jsonPath("$.name").value(changeTo.getName()))
                .andDo(
                        document("club/update/club",
                                pathParameters(
                                        parameterWithName("clubId").description("대상 동아리 ID").attributes(example("1"))
                                ),
                                queryParameters(
                                        parameterWithName("clubName").description("동아리 이름").attributes(example("클럽 SKKULOL")),
                                        parameterWithName("briefActivityDescription").description(" 분류 - 활동 설명").attributes(example("E-SPORTS")),
                                        parameterWithName("activityDescription").description("자세한 활동 설명").attributes(example("1. 열심히 참여하면 됩니다 2. 그냥 게임만 잘 하면 됩니다.")),
                                        parameterWithName("clubDescription").description("자세한 동아리 설명").attributes(example("여기가 어떤 동아리냐면요, 페이커가 될 수 있게 해주는 동아리입니다^^")),
                                        parameterWithName("establishDate").description("설립 연도, Integer(Min 1398)").optional().attributes(example("2023")),
                                        parameterWithName("headLine").description("한줄 소개").optional().attributes(example("명륜 게임 동아리입니다")),
                                        parameterWithName("mandatoryActivatePeriod").description("의무 활동 기간").optional().attributes(example("4학기")),
                                        parameterWithName("memberAmount").description("동아리 인원").optional().attributes(example("60")),
                                        parameterWithName("regularMeetingTime").description("정규 모임 시간").optional().attributes(example("Thursday 19:00")),
                                        parameterWithName("roomLocation").description("동아리 방 위치").optional().attributes(example("학생회관 80210")),
                                        parameterWithName("webLink1").description("관련 사이트 주소 1").optional().attributes(example("www.skklol.edu")),
                                        parameterWithName("webLink2").description("관련 사이트 주소 2").optional().attributes(example("skklol.com"))
                                ),
                                responseFields(
                                        fieldWithPath("id").type(WireFormat.FieldType.INT64).description("수정된 동아리 ID").attributes(example("1"))
                                        , fieldWithPath("name").type(WireFormat.FieldType.STRING).description("수정된 동아리 이름").attributes(example("클럽 SKKULOL"))
                                )
                        )
                );

    }

    @Test
    public void updateClub_IllegalClubId_ClubIdMisMatchException() throws Exception {
        //given
        Long clubId = -1L;
        Club club = testDataRepository.getClubs().get(0);
        doNothing().when(authValidator).validateUpdatingClub(clubId);
        given(clubService.updateClub(eq(clubId), any(Club.class))).willReturn(Optional.empty());

        //when
        MvcResult badIdResult = mockMvc.perform(
                patch("/club/{clubId}", clubId)
                        .with(csrf())
                        .queryParam("clubName", club.getName())
                        .queryParam("campus", club.getCampus().toString())
                        .queryParam("clubType", club.getClubType().toString())
                        .queryParam("belongs", club.getBelongs())
                        .queryParam("briefActivityDescription", club.getBriefActivityDescription())
                        .queryParam("activityDescription", club.getActivityDescription())
                        .queryParam("clubDescription", club.getClubDescription())
                        .queryParam("establishDate", club.getEstablishAt().toString())
                        .queryParam("headLine", club.getHeadLine())
                        .queryParam("mandatoryActivatePeriod", club.getMandatoryActivatePeriod())
                        .queryParam("memberAmount", club.getMemberAmount().toString())
                        .queryParam("regularMeetingTime", club.getRegularMeetingTime())
                        .queryParam("roomLocation", club.getRoomLocation())
                        .queryParam("webLink1", club.getWebLink1())
                        .queryParam("webLink2", club.getWebLink2())
        ).andExpect(status().isBadRequest()).andReturn();

        //then
        Assertions.assertThat(badIdResult.getResolvedException()).isExactlyInstanceOf(ClubIdMisMatchException.class);
    }

    @Test
    public void updateLogo_ToNewLogo_Success() throws Exception {
        Long clubId = 0L;
        String oldLogoName = "savedOldLogo.jpg";
        FileNames fileNames = new FileNames("TestLogo.jpg", "savedTestLogo.jpg");
        Logo logo = fileNames.toLogoEntity();
        doNothing().when(authValidator).validateUpdatingClub(clubId);
        given(s3Transferer.uploadOne(mockLogo)).willReturn(fileNames);
        given(clubService.updateLogo(clubId, logo)).willReturn(Optional.of(oldLogoName));
        doNothing().when(s3Transferer).deleteOne(oldLogoName);

        //when
        ResultActions actions = mockMvc.perform(
                multipart("/club/{clubId}/logo", clubId)
                        .file(mockLogo)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA)
        );

        //then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.clubId").value(clubId.toString()))
                .andExpect(jsonPath("$.logoOriginalName").value(fileNames.getOriginalName()))
                .andExpect(jsonPath("$.logoSavedName").value(fileNames.getSavedName()))
                .andDo(
                        document(
                                "club/update/logo"
                                , pathParameters(
                                        parameterWithName("clubId").description("동아리 ID").attributes(example("0"))
                                ),
                                requestParts(
                                        partWithName("logo").description("동아리 로고")
                                ),
                                responseFields(
                                        fieldWithPath("clubId").description("동아리 아이디").type(WireFormat.FieldType.INT64).attributes(example(clubId.toString())),
                                        fieldWithPath("logoOriginalName").description("반영된 로고 파일명").type(WireFormat.FieldType.STRING).attributes(example(fileNames.getOriginalName())),
                                        fieldWithPath("logoSavedName").description("반영된 로고 파일 저장명").type(WireFormat.FieldType.STRING).attributes(example(fileNames.getSavedName()))
                                )
                        )
                );

    }

    @Test
    public void updateLogo_DefaultToNewLogo_Success() throws Exception {
        //given
        Long clubId = 0L;
        String oldLogoName = "alt.jpg";
        FileNames fileNames = new FileNames("TestLogo.jpg", "savedTestLogo.jpg");
        doNothing().when(authValidator).validateUpdatingClub(clubId);
        given(s3Transferer.uploadOne(mockLogo)).willReturn(fileNames);
        given(clubService.updateLogo(clubId, fileNames.toLogoEntity())).willReturn(Optional.of(oldLogoName));
        doThrow(IllegalCallerException.class).when(s3Transferer).deleteOne(anyString());

        //when
        MvcResult mvcResult = mockMvc.perform(
                        multipart("/club/{clubId}/logo", clubId)
                                .file(mockLogo)
                                .with(csrf())
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.clubId").value(clubId.toString()))
                .andExpect(jsonPath("$.logoOriginalName").value(fileNames.getOriginalName()))
                .andExpect(jsonPath("$.logoSavedName").value(fileNames.getSavedName()))
                .andReturn();

        //then
        Assertions.assertThat(mvcResult.getResolvedException()).doesNotThrowAnyException();
    }

    @Test
    public void updateLogo_IllegalClubId_ClubMisMatchException() throws Exception {
        //given
        Long clubId = -1L;
        FileNames fileNames = new FileNames("TestLogo.jpg", "savedTestLogo.jpg");
        doNothing().when(authValidator).validateUpdatingClub(clubId);
        given(s3Transferer.uploadOne(mockLogo)).willReturn(fileNames);
        given(clubService.updateLogo(clubId, fileNames.toLogoEntity())).willReturn(Optional.empty());

        //when
        MvcResult badIdResult = mockMvc.perform(
                multipart("/club/{clubId}/logo", clubId)
                        .file(mockLogo)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA)
        ).andExpect(status().isBadRequest()).andReturn();

        //then
        Assertions.assertThat(badIdResult.getResolvedException()).isExactlyInstanceOf(ClubIdMisMatchException.class);

    }

    @Test
    public void updateLogo_NoLogoFile_MissingServletRequestParameterException() throws Exception {
        //given
        Long clubId = 0L;
        doNothing().when(authValidator).validateUpdatingClub(clubId);

        //when
        MvcResult noLogoResult = mockMvc.perform(
                multipart("/club/{clubId}/logo", clubId)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA)
        ).andExpect(status().isBadRequest()).andReturn();

        //then
        Assertions.assertThat(noLogoResult.getResolvedException()).isExactlyInstanceOf(MissingServletRequestPartException.class);

    }
    
    @Test
    public void downGradeClub_Given중앙동아리_To준중앙동아리() throws Exception{
        //given
        Long clubId = 31L;
        Club club = testDataRepository.getCleanClub(1);
        setClubId(club, clubId);
        changeClubType(club, ClubType.중앙동아리);
        doNothing().when(authValidator).validateUpdatingClub(clubId);
        doAnswer(
                invocation -> {
                    changeClubType(club,ClubType.준중앙동아리);
                    return Optional.of(club);
                }
        ).when(clubService).downGrade(clubId);

        //when
        ResultActions actions = mockMvc.perform(
                patch("/club/{clubId}/down", clubId)
                        .with(csrf())
        );

        //then
        Assertions.assertThat(club.getClubType()).isEqualTo(ClubType.준중앙동아리);
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.clubId").value(clubId))
                .andExpect(jsonPath("$.clubName").value(club.getName()))
                .andExpect(jsonPath("$.campus").value(club.getCampus().toString()))
                .andExpect(jsonPath("$.clubType").value(club.getClubType().toString()))
                .andExpect(jsonPath("$.belongs").value(club.getBelongs()))
                .andExpect(jsonPath("$.briefDescription").value(club.getBriefActivityDescription()))
                .andDo(
                        document(
                                "club/update/grade/down"
                                , pathParameters(
                                        parameterWithName("clubId").description("중앙동아리 ID").attributes(example("1"))
                                )
                                , responseFields(
                                        fieldWithPath("clubId").type(WireFormat.FieldType.INT64).description("강등된 동아리 ID").attributes(example("1")),
                                        fieldWithPath("clubName").type(WireFormat.FieldType.STRING).description("동아리 이름").attributes(example(club.getName())),
                                        fieldWithPath("campus").type(WireFormat.FieldType.STRING).description("소속 캠퍼스").attributes(example(LINK_CAMPUS_TYPE)),
                                        fieldWithPath("clubType").type(WireFormat.FieldType.STRING).description("준중앙동아리").attributes(example(club.getClubType().toString())),
                                        fieldWithPath("belongs").type(WireFormat.FieldType.STRING).description("소속 분과").attributes(example(RestDocsUtils.LINK_BELONGS_TYPE)),
                                        fieldWithPath("briefDescription").type(WireFormat.FieldType.STRING).description("세부 분류").attributes(example(club.getBriefActivityDescription()))
                                )
                        )
                );
    }

    @Test
    public void downGradeClub_BadClubType_CannotDownGradeClubException() throws Exception{
        //given
        Long clubId = 31L;
        Club club = testDataRepository.getCleanClub(1);
        setClubId(club, clubId);
        changeClubType(club, ClubType.중앙동아리);
        doNothing().when(authValidator).validateUpdatingClub(clubId);
        given(clubService.downGrade(clubId)).willThrow(CannotDownGradeClubException.class);

        //when
        MvcResult badClubTypeResult = mockMvc.perform(
                patch("/club/{clubId}/down", clubId)
                        .with(csrf())
        ).andExpect(status().isBadRequest()).andReturn();

        //then
        Assertions.assertThat(badClubTypeResult.getResolvedException()).isExactlyInstanceOf(CannotDownGradeClubException.class);
    }

    @Test
    public void downGradeClub_BadClubId_ClubIdMisMatchException() throws Exception{
        //given
        Long clubId = 31L;
        doNothing().when(authValidator).validateUpdatingClub(clubId);
        given(clubService.downGrade(clubId)).willReturn(Optional.empty());

        //when
        MvcResult badClubIdResult = mockMvc.perform(
                patch("/club/{clubId}/down", clubId)
                        .with(csrf())
        ).andExpect(status().isBadRequest()).andReturn();

        //then
        Assertions.assertThat(badClubIdResult.getResolvedException()).isExactlyInstanceOf(ClubIdMisMatchException.class);
    }

    @Test
    public void upGradeClub_Given준중앙동아리_To중앙동아리() throws Exception{
        //given
        Long clubId = 31L;
        Club club = testDataRepository.getCleanClub(1);
        setClubId(club, clubId);
        changeClubType(club, ClubType.준중앙동아리);
        doNothing().when(authValidator).validateUpdatingClub(clubId);
        doAnswer(
                invocation -> {
                    changeClubType(club,ClubType.중앙동아리);
                    return Optional.of(club);
                }
        ).when(clubService).upGrade(clubId);

        //when
        ResultActions actions = mockMvc.perform(
                patch("/club/{clubId}/up", clubId)
                        .with(csrf())
        );

        //then
        Assertions.assertThat(club.getClubType()).isEqualTo(ClubType.중앙동아리);
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.clubId").value(clubId))
                .andExpect(jsonPath("$.clubName").value(club.getName()))
                .andExpect(jsonPath("$.campus").value(club.getCampus().toString()))
                .andExpect(jsonPath("$.clubType").value(club.getClubType().toString()))
                .andExpect(jsonPath("$.belongs").value(club.getBelongs()))
                .andExpect(jsonPath("$.briefDescription").value(club.getBriefActivityDescription()))
                .andDo(
                        document(
                                "club/update/grade/up"
                                , pathParameters(
                                        parameterWithName("clubId").description("준중앙동아리 ID").attributes(example("1"))
                                )
                                , responseFields(
                                        fieldWithPath("clubId").type(WireFormat.FieldType.INT64).description("승격된 동아리 ID").attributes(example("1")),
                                        fieldWithPath("clubName").type(WireFormat.FieldType.STRING).description("동아리 이름").attributes(example(club.getName())),
                                        fieldWithPath("campus").type(WireFormat.FieldType.STRING).description("소속 캠퍼스").attributes(example(LINK_CAMPUS_TYPE)),
                                        fieldWithPath("clubType").type(WireFormat.FieldType.STRING).description("중앙동아리").attributes(example(club.getClubType().toString())),
                                        fieldWithPath("belongs").type(WireFormat.FieldType.STRING).description("소속 분과").attributes(example(LINK_BELONGS_TYPE)),
                                        fieldWithPath("briefDescription").type(WireFormat.FieldType.STRING).description("세부 분류").attributes(example(club.getBriefActivityDescription()))
                                )
                        )
                );
    }

    @Test
    public void upGradeClub_BadClubType_CannotUpGradeClubException() throws Exception{
        //given
        Long clubId = 31L;
        Club club = testDataRepository.getCleanClub(1);
        setClubId(club, clubId);
        changeClubType(club, ClubType.준중앙동아리);
        doNothing().when(authValidator).validateUpdatingClub(clubId);
        given(clubService.upGrade(clubId)).willThrow(CannotUpGradeClubException.class);

        //when
        MvcResult badClubTypeResult = mockMvc.perform(
                patch("/club/{clubId}/up", clubId)
                        .with(csrf())
        ).andExpect(status().isBadRequest()).andReturn();

        //then
        Assertions.assertThat(badClubTypeResult.getResolvedException()).isExactlyInstanceOf(CannotUpGradeClubException.class);
    }

    @Test
    public void upGradeClub_BadClubId_ClubIdMisMatchException() throws Exception{
        Long clubId = 31L;
        doNothing().when(authValidator).validateUpdatingClub(clubId);
        given(clubService.upGrade(clubId)).willReturn(Optional.empty());

        //when
        MvcResult badClubIdResult = mockMvc.perform(
                patch("/club/{clubId}/up", clubId)
                        .with(csrf())
        ).andExpect(status().isBadRequest()).andReturn();

        //then
        Assertions.assertThat(badClubIdResult.getResolvedException()).isExactlyInstanceOf(ClubIdMisMatchException.class);
    }


    private void setClubId(Club club, Long clubId) throws NoSuchFieldException, IllegalAccessException {
        Field clubIdField = club.getClass().getDeclaredField("id");
        clubIdField.setAccessible(true);
        clubIdField.set(club, clubId);
    }

    private void changeClubType(Club club, ClubType clubType) throws NoSuchFieldException, IllegalAccessException {
        Field clubTypeField = club.getClass().getDeclaredField("clubType");
        clubTypeField.setAccessible(true);
        clubTypeField.set(club, clubType);
    }
}