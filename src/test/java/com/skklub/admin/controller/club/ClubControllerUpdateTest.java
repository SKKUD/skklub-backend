package com.skklub.admin.controller.club;


import akka.protobuf.WireFormat;
import com.skklub.admin.controller.ClubController;
import com.skklub.admin.controller.ClubTestDataRepository;
import com.skklub.admin.controller.RestDocsUtils;
import com.skklub.admin.controller.S3Transferer;
import com.skklub.admin.domain.Club;
import com.skklub.admin.domain.enums.Campus;
import com.skklub.admin.domain.enums.ClubType;
import com.skklub.admin.error.exception.ClubIdMisMatchException;
import com.skklub.admin.error.exception.InvalidBelongsException;
import com.skklub.admin.service.ClubService;
import com.skklub.admin.service.dto.FileNames;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import java.util.Optional;

import static com.skklub.admin.controller.RestDocsUtils.example;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
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
@Import(ClubTestDataRepository.class)
@WebMvcTest(controllers = ClubController.class)
@MockBean(JpaMetamodelMappingContext.class)
class ClubControllerUpdateTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ClubService clubService;
    @MockBean
    private S3Transferer s3Transferer;
    @Autowired
    private ClubTestDataRepository clubTestDataRepository;

    private MockMultipartFile mockLogo;

    @BeforeEach
    public void beforeEach() throws IOException {
        mockLogo = new MockMultipartFile(
                "logo",
                "test.png",
                ContentType.MULTIPART_FORM_DATA.toString(),
                new FileInputStream("src/main/resources/2020-12-25 (5).png")
        );
    }

    @Test
    public void updateClub_Default_Success() throws Exception {
        //given
        Long clubId = 0L;
        Long changeToId = 1L;
        Club club = clubTestDataRepository.getClubs().get(clubId.intValue());
        Club changeTo = clubTestDataRepository.getClubs().get(changeToId.intValue());
        given(clubService.updateClub(eq(clubId), any(Club.class))).willReturn(Optional.ofNullable(changeTo.getName()));

        //when
        ResultActions actions = mockMvc.perform(
                patch("/club/{clubId}", clubId)
                        .with(csrf())
                        .queryParam("clubName", changeTo.getName())
                        .queryParam("campus", changeTo.getCampus().toString())
                        .queryParam("clubType", changeTo.getClubType().toString())
                        .queryParam("belongs", changeTo.getBelongs())
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
                                        parameterWithName("campus").description("분류 - 캠퍼스").attributes(example(RestDocsUtils.LINK_CAMPUS_TYPE)),
                                        parameterWithName("clubType").description("분류 - 동아리 종류").attributes(example(RestDocsUtils.LINK_CLUB_TYPE)),
                                        parameterWithName("belongs").description("분류 - 동아리 분과").attributes(example(RestDocsUtils.LINK_BELONGS_TYPE)),
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
    public void updateClub_IllegalBelongs_InvalidBelongsException() throws Exception {
        //given
        Long clubId = 0L;
        Long changeToId = 1L;
        Club changeTo = clubTestDataRepository.getClubs().get(changeToId.intValue());

        Campus campus = Campus.명륜;
        ClubType clubType = ClubType.중앙동아리;
        String belongs = "IllegalBelongs";

        //when
        MvcResult badBelongsResult = mockMvc.perform(
                patch("/club/{clubId}", clubId)
                        .with(csrf())
                        .queryParam("clubName", changeTo.getName())
                        .queryParam("campus", campus.toString())
                        .queryParam("clubType", clubType.toString())
                        .queryParam("belongs", belongs)
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
        ).andReturn();

        //then
        Assertions.assertThat(badBelongsResult.getResolvedException()).isExactlyInstanceOf(InvalidBelongsException.class);

    }

    @Test
    public void updateClub_IllegalClubId_ClubIdMisMatchException() throws Exception {
        //given
        Long clubId = -1L;
        Club club = clubTestDataRepository.getClubs().get(0);
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
        ).andReturn();

        //then
        Assertions.assertThat(badIdResult.getResolvedException()).isExactlyInstanceOf(ClubIdMisMatchException.class);
    }

    @Test
    public void updateLogo_ToNewLogo_Success() throws Exception {
        Long clubId = 0L;
        String oldLogoName = "savedOldLogo.jpg";
        FileNames fileNames = new FileNames("TestLogo.jpg", "savedTestLogo.jpg");
        given(s3Transferer.uploadOne(mockLogo)).willReturn(fileNames);
        given(clubService.updateLogo(clubId, fileNames)).willReturn(Optional.of(oldLogoName));
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
        given(s3Transferer.uploadOne(mockLogo)).willReturn(fileNames);
        given(clubService.updateLogo(clubId, fileNames)).willReturn(Optional.of(oldLogoName));
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
        given(s3Transferer.uploadOne(mockLogo)).willReturn(fileNames);
        given(clubService.updateLogo(clubId, fileNames)).willReturn(Optional.empty());

        //when
        MvcResult badIdResult = mockMvc.perform(
                multipart("/club/{clubId}/logo", clubId)
                        .file(mockLogo)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA)
        ).andReturn();

        //then
        Assertions.assertThat(badIdResult.getResolvedException()).isExactlyInstanceOf(ClubIdMisMatchException.class);

    }

    @Test
    public void updateLogo_NoLogoFile_MissingServletRequestParameterException() throws Exception {
        //given
        Long clubId = 0L;

        //when
        MvcResult noLogoResult = mockMvc.perform(
                multipart("/club/{clubId}/logo", clubId)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA)
        ).andReturn();

        //then
        Assertions.assertThat(noLogoResult.getResolvedException()).isExactlyInstanceOf(MissingServletRequestPartException.class);

    }
}