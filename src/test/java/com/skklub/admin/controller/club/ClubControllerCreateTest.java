package com.skklub.admin.controller.club;

import com.skklub.admin.controller.ClubController;
import com.skklub.admin.controller.ClubTestDataRepository;
import com.skklub.admin.controller.RestDocsUtils;
import com.skklub.admin.controller.S3Transferer;
import com.skklub.admin.controller.error.exception.InvalidBelongsException;
import com.skklub.admin.domain.Club;
import com.skklub.admin.domain.enums.Campus;
import com.skklub.admin.domain.enums.ClubType;
import com.skklub.admin.service.ClubService;
import com.skklub.admin.service.dto.FileNames;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static akka.protobuf.WireFormat.FieldType;
import static com.skklub.admin.controller.RestDocsUtils.example;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willReturn;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.multipart;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import(ClubTestDataRepository.class)
@WebMvcTest(controllers = ClubController.class)
@MockBean(JpaMetamodelMappingContext.class)
@WithMockUser
class ClubControllerCreateTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ClubService clubService;
    @MockBean
    private S3Transferer s3Transferer;
    @Autowired
    private ClubTestDataRepository clubTestDataRepository;

    private MockMultipartFile mockLogo;
    private List<MockMultipartFile> mockActivityImages = new ArrayList<>();

    @PostConstruct
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
                        .with(csrf())
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
                .andDo(document("club/create/club",
                        queryParameters(
                                parameterWithName("clubName").description("동아리 이름").attributes(example("클럽 SKKULOL")),
                                parameterWithName("campus").description("분류 - 캠퍼스").attributes(example(RestDocsUtils.LINK_CAMPUS_TYPE)),
                                parameterWithName("clubType").description("분류 - 동아리 종류").attributes(example(RestDocsUtils.LINK_CLUB_TYPE)),
                                parameterWithName("belongs").description("분류 - 동아리 분과").attributes(example(RestDocsUtils.LINK_BELONGS_TYPE)),
                                parameterWithName("briefActivityDescription").description("분류 - 활동 설명").attributes(example("E-SPORTS")),
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
                        requestParts(
                                partWithName("logo").description("동아리 로고").optional()
                        ),
                        responseFields(
                                fieldWithPath("id").type(FieldType.STRING).description("동아리 아이디").attributes(example("0")),
                                fieldWithPath("name").type(FieldType.STRING).description("동아리명").attributes(example("클럽 SKKULOL"))
                        )

                ));
    }

    @Test
    public void clubCreation_NullAtSomeNullables_Success() throws Exception {
        //given
        Long clubId = 0L;
        clubTestDataRepository.getClubs().get(0);
        FileNames logoFileName = clubTestDataRepository.getLogoFileName((int) (long)clubId);
        given(s3Transferer.uploadOne(any(MultipartFile.class))).willReturn(logoFileName);
        given(clubService.createClub(any(Club.class), eq(logoFileName.getOriginalName()), eq(logoFileName.getSavedName()))).willReturn(clubId);

        //when
        ResultActions actions = mockMvc.perform(
                multipart("/club")
                        .file(mockLogo)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(csrf())
                        .queryParam("clubName", "정상적인 클럽 SKKULOL")
                        .queryParam("campus", "명륜")
                        .queryParam("clubType", "중앙동아리")
                        .queryParam("belongs", "취미교양")
                        .queryParam("briefActivityDescription", "E-SPORTS")
                        .queryParam("activityDescription", "1. 열심히 참여하면 됩니다 2. 그냥 게임만 잘 하면 됩니다.")
                        .queryParam("clubDescription", "여기가 어떤 동아리냐면요, 페이커가 될 수 있게 해주는 동아리입니다^^")
                        .queryParam("establishDate", "") // Blank
//                        .queryParam("headLine", )
//                        .queryParam("mandatoryActivatePeriod", "4학기") // missing field
//                        .queryParam("memberAmount", "60")
//                        .queryParam("regularMeetingTime", "Thursday 19:00")
//                        .queryParam("roomLocation", "학생회관 80210")
//                        .queryParam("webLink1", "www.skklol.com")
//                        .queryParam("webLink2", "www.skkulol.edu")
        );

        //then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clubId))
                .andExpect(jsonPath("$.name").value("정상적인 클럽 SKKULOL"));
    }

     @Test
     public void clubCreation_EnumMisMatch_MethodArgumentNotValidException() throws Exception{
         //given
         String wrongCampus = "안암";
         String wrongClubType = "중준중동아리";

         //when
         MvcResult wrongClubTypeResult = mockMvc.perform(
                 multipart("/club")
                         .file(mockLogo)
                         .contentType(MediaType.MULTIPART_FORM_DATA)
                         .with(csrf())
                         .queryParam("clubName", "정상적인 클럽 SKKULOL")
                         .queryParam("campus", Campus.명륜.toString())
                         .queryParam("clubType", wrongClubType)
                         .queryParam("belongs", "취미교양")
                         .queryParam("briefActivityDescription", "E-SPORTS")
                         .queryParam("activityDescription", "1. 열심히 참여하면 됩니다 2. 그냥 게임만 잘 하면 됩니다.")
                         .queryParam("clubDescription", "여기가 어떤 동아리냐면요, 페이커가 될 수 있게 해주는 동아리입니다^^")
         ).andReturn();
         MvcResult wrongCampusResult = mockMvc.perform(
                 multipart("/club")
                         .file(mockLogo)
                         .contentType(MediaType.MULTIPART_FORM_DATA)
                         .with(csrf())
                         .queryParam("clubName", "정상적인 클럽 SKKULOL")
                         .queryParam("campus", wrongCampus)
                         .queryParam("clubType", "중앙동아리")
                         .queryParam("belongs", "취미교양")
                         .queryParam("briefActivityDescription", "E-SPORTS")
                         .queryParam("activityDescription", "1. 열심히 참여하면 됩니다 2. 그냥 게임만 잘 하면 됩니다.")
                         .queryParam("clubDescription", "여기가 어떤 동아리냐면요, 페이커가 될 수 있게 해주는 동아리입니다^^")
         ).andReturn();

         //then
         Assertions.assertThat(wrongCampusResult.getResolvedException()).isInstanceOf(BindException.class);
         Assertions.assertThat(wrongClubTypeResult.getResolvedException()).isInstanceOf(BindException.class);

      }

    @Test
    public void clubCreation_EmptyLogoImage_FileNameEqDefaultLogo() throws Exception {
        //given
        Long successId = 12345L;
        given(clubService.createClub(any(Club.class), eq("alt.jpg"), anyString())).willReturn(successId);

        //when
        ResultActions actions = mockMvc.perform(
                multipart("/club")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(csrf())
                        .queryParam("clubName", "정상적인 클럽 SKKULOL")
                        .queryParam("campus", Campus.명륜.toString())
                        .queryParam("clubType", "중앙동아리")
                        .queryParam("belongs", "취미교양")
                        .queryParam("briefActivityDescription", "E-SPORTS")
                        .queryParam("activityDescription", "1. 열심히 참여하면 됩니다 2. 그냥 게임만 잘 하면 됩니다.")
                        .queryParam("clubDescription", "여기가 어떤 동아리냐면요, 페이커가 될 수 있게 해주는 동아리입니다^^")
        );

        //then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(successId))
                .andExpect(jsonPath("$.name").value("정상적인 클럽 SKKULOL"));

    }

    @Test
    public void clubCreation_InvalidBelongs_InvalidBelongsException() throws Exception {
        //given
        Campus campus = Campus.명륜;
        ClubType clubType = ClubType.중앙동아리;
        String suwonCentralBelongs = "건강체육";

        //when
        MvcResult wrongBelongsResult = mockMvc.perform(
                multipart("/club")
                        .file(mockLogo)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(csrf())
                        .queryParam("clubName", "정상적인 클럽 SKKULOL")
                        .queryParam("campus", campus.toString())
                        .queryParam("clubType", clubType.toString())
                        .queryParam("belongs", suwonCentralBelongs)
                        .queryParam("briefActivityDescription", "E-SPORTS")
                        .queryParam("activityDescription", "1. 열심히 참여하면 됩니다 2. 그냥 게임만 잘 하면 됩니다.")
                        .queryParam("clubDescription", "여기가 어떤 동아리냐면요, 페이커가 될 수 있게 해주는 동아리입니다^^")
        ).andReturn();

        //then
        Assertions.assertThat(wrongBelongsResult.getResolvedException()).isInstanceOf(InvalidBelongsException.class);

    }

    @Test
    public void uploadActivityImages_MultiImages_Success() throws Exception {
        //given
        List<MultipartFile> multipartFiles = new ArrayList<>();
        List<FileNames> activityImageDtos = new ArrayList<>();
        given(s3Transferer.uploadAll(multipartFiles)).willReturn(activityImageDtos);
        given(clubService.appendActivityImages(0L, activityImageDtos)).willReturn(Optional.of("ClubName"));

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
                        .with(csrf())
        );

        //then

        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(0L))
                .andExpect(jsonPath("$.name").value("ClubName"))
                .andDo(document("club/create/activityImages",
                        pathParameters(
                                parameterWithName("clubId").description("동아리 ID").attributes(example("1"))
                        ),
                        requestParts(
                                partWithName("activityImages").description("활동 사진").optional()
                        ),
                        responseFields(
                                fieldWithPath("id").type(FieldType.STRING).description("동아리 ID").attributes(example("0")),
                                fieldWithPath("name").type(FieldType.STRING).description("동아리 이름").attributes(example("클럽 SKKULOL"))
                        )
                ));
    }

    @Test
    public void uploadActivityImages_EmptyList_Fail() throws Exception {
        //given
        List<MultipartFile> multipartFiles = new ArrayList<>();
        List<FileNames> activityImageDtos = new ArrayList<>();
        given(s3Transferer.uploadAll(multipartFiles)).willReturn(activityImageDtos);

        //when
        ResultActions actions = mockMvc.perform(
                multipart("/club/{clubId}/activityImage", 0L)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(csrf())
        );

        //then

        actions.andExpect(status().is4xxClientError());
    }

}