package com.skklub.admin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skklub.admin.controller.dto.S3DownloadDto;
import com.skklub.admin.domain.Club;
import com.skklub.admin.repository.ClubRepository;
import com.skklub.admin.service.ClubService;
import com.skklub.admin.service.dto.ClubDetailInfoDto;
import com.skklub.admin.service.dto.FileNames;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import static akka.protobuf.WireFormat.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.multipart;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@WebMvcTest(controllers = ClubController.class)
@MockBean(JpaMetamodelMappingContext.class)
class ClubControllerCreateTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ClubService clubService;
    @MockBean
    private S3Transferer s3Transferer;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMultipartFile mockLogo;
    private List<MockMultipartFile> mockActivityImages = new ArrayList<>();

    @BeforeEach
    public void beforeEach() throws Exception {
        mockLogo = new MockMultipartFile(
                "logo",
                "test.png",
                ContentType.MULTIPART_FORM_DATA.toString(),
                new FileInputStream("src/main/resources/2020-12-25 (5).png")
        );

        for (int i = 0; i < 10; i++) {
            mockActivityImages.add(new MockMultipartFile(
                    "activityImages",
                    "test" + i + ".png",
                    ContentType.MULTIPART_FORM_DATA.toString(),
                    new FileInputStream("src/main/resources/2020-12-25 (5).png")
            ));
        }
    }

    @AfterEach
    public void afterEach() throws Exception {
        mockLogo = null;
        mockActivityImages = new ArrayList<>();
    }

    @Test
    public void clubCreation_FullData_Success() throws Exception {
        //given
        given(clubService.createClub(any(Club.class), anyString(), anyString())).willReturn(0L);
        given(s3Transferer.uploadOne(any(MockMultipartFile.class))).willReturn(new FileNames("test.png", "saved-test.png"));
        //when
        ResultActions actions = mockMvc.perform(
                multipart("/club")
                        .file(mockLogo)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .queryParam("clubName", "정상적인 클럽 SKKULOL")
                        .queryParam("campus", "명륜")
                        .queryParam("clubType", "중앙동아리")
                        .queryParam("belongs", "취미교양")
                        .queryParam("briefActivityDescription", "E-SPORTS")
                        .queryParam("activityDescription", "1. 열심히 참여하면 됩니다 2. 그냥 게임만 잘 하면 됩니다.")
                        .queryParam("clubDescription", "여기가 어떤 동아리냐면요, 페이커가 될 수 있게 해주는 동아리입니다^^")
                        .queryParam("establishDate", "2023")
                        .queryParam("headLine", "명륜 게임 동아리입니다")
                        .queryParam("mandatoryActivatePeriod", "4학기")
                        .queryParam("memberAmount", "60")
                        .queryParam("regularMeetingTime", "Thursday 19:00")
                        .queryParam("roomLocation", "학생회관 80210")
                        .queryParam("webLink1", "www.skklol.com")
                        .queryParam("webLink2", "www.skkulol.edu")
        );

        //then
        actions
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.id", 0L).exists())
                .andExpect(jsonPath("$.name", "정상적인 클럽 SKKULOL").exists())
                .andDo(document("/club/creation",
                        queryParameters(
                                parameterWithName("clubName").description("동아리 이름"),
                                parameterWithName("campus").description("분류 - 캠퍼스"),
                                parameterWithName("clubType").description("분류 - 동아리 종류"),
                                parameterWithName("belongs").description("분류 - 동아리 분과"),
                                parameterWithName("briefActivityDescription").description(" 분류 - 활동 설명"),
                                parameterWithName("activityDescription").description("자세한 활동 설명"),
                                parameterWithName("clubDescription").description("자세한 동아리 설명"),
                                parameterWithName("establishDate").description("설립 연도"),
                                parameterWithName("headLine").description("한줄 소개"),
                                parameterWithName("mandatoryActivatePeriod").description("의무 활동 기간"),
                                parameterWithName("memberAmount").description("동아리 인원"),
                                parameterWithName("regularMeetingTime").description("정규 모임 시간"),
                                parameterWithName("roomLocation").description("동아리 방 위치"),
                                parameterWithName("webLink1").description("관련 사이트 주소 1"),
                                parameterWithName("webLink2").description("관련 사이트 주소 2")
                        ),
                        requestParts(
                                partWithName("logo").description("동아리 로고")
                        ),
                        responseFields(
                                fieldWithPath("id").type(FieldType.STRING).description("동아리 아이디"),
                                fieldWithPath("name").type(FieldType.STRING).description("동아리명")
                        )

                ));
    }

    @Test
    public void clubCreation_NullAtNullables_Success() throws Exception {
        //given

        //when

        //then

    }

    @Test
    public void clubCreation_NullAtNotNulls_Fail() throws Exception {
        //given

        //when

        //then

    }

    @Test
    public void clubCreation_EmptyLogoImage_GetDefaultLogo() throws Exception {
        //given

        //when

        //then

    }

    @Test
    public void clubCreation_SameLogoFileName_Success() throws Exception {
        //given

        //when

        //then

    }

    @Test
    public void uploadActivityImages_MultiImages_Success() throws Exception {
        //given
        List<MultipartFile> multipartFiles = new ArrayList<>();
        List<FileNames> activityImageDtos = new ArrayList<>();
        given(s3Transferer.uploadAll(multipartFiles)).willReturn(activityImageDtos);
        given(clubService.appendActivityImages(0L, activityImageDtos)).willReturn("ClubName");

        //when
        ResultActions actions = mockMvc.perform(
                multipart("/club/{clubId}/activityImage", 0L)
                        .file(mockActivityImages.get(0))
                        .file(mockActivityImages.get(1))
                        .file(mockActivityImages.get(2))
                        .file(mockActivityImages.get(3))
                        .file(mockActivityImages.get(4))
                        .file(mockActivityImages.get(5))
                        .file(mockActivityImages.get(6))
                        .file(mockActivityImages.get(7))
                        .file(mockActivityImages.get(8))
                        .file(mockActivityImages.get(9))
                        .contentType(MediaType.MULTIPART_FORM_DATA)
        );

        //then

        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.id", 0L).exists())
                .andExpect(jsonPath("$.name", "ClubName").exists())
                .andDo(document("/club/{clubId}/activityImages",
                        pathParameters(
                                parameterWithName("clubId").description("동아리 ID")
                        ),
                        requestParts(
                                partWithName("activityImages").description("활동 사진")
                        ),
                        responseFields(
                                fieldWithPath("id").type(FieldType.STRING).description("동아리 ID"),
                                fieldWithPath("name").type(FieldType.STRING).description("동아리 이름")
                        )
                ));
    }

    @Test
    public void uploadActivityImages_EmptyList_Success() throws Exception {
        //given

        //when

        //then

    }

}