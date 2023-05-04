package com.skklub.admin.controller;

import akka.protobuf.WireFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skklub.admin.controller.dto.S3DownloadDto;
import com.skklub.admin.domain.*;
import com.skklub.admin.domain.enums.ActivityType;
import com.skklub.admin.domain.enums.Campus;
import com.skklub.admin.domain.enums.ClubType;
import com.skklub.admin.domain.enums.College;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.io.FileInputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.time.LocalTime.now;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@WebMvcTest(controllers = ClubController.class)
@MockBean(JpaMetamodelMappingContext.class)
class ClubControllerReadTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ClubService clubService;

    @MockBean
    private S3Transferer s3Transferer;

    @Autowired
    private ObjectMapper objectMapper;

    private FileNames fileName;
    private List<FileNames> fileNames = new ArrayList<>();
    private ClubDetailInfoDto clubDetailInfoDto;

    @BeforeEach
    public void beforeEach() throws Exception {
        fileName = new FileNames("originalLogo.png", "savedLogo.png");
        fileNames.add(new FileNames("originalAc1.png", "uploadedAc1.png"));
        fileNames.add(new FileNames("originalAc2.png", "uploadedAc2.png"));
        fileNames.add(new FileNames("originalAc3.png", "uploadedAc3.png"));
        fileNames.add(new FileNames("originalAc4.png", "uploadedAc4.png"));
        Club club = new Club("name","activity_description", ActivityType.취미교양, ClubType.중앙동아리,"brief_activity_description",
                Campus.명륜, "club_description", College.경영대학,  "1234", "head_line", "mandatory_activate_period", 60,
                 "regular_meeting_time", "room_location", "web_link1", "web_link2");
        Logo logo = new Logo(fileName.getOriginalName(), fileName.getSavedName());
        List<ActivityImage> activityImages = fileNames.stream().map(f -> new ActivityImage(f.getOriginalName(), f.getSavedName())).collect(Collectors.toList());
        Recruit recruit = new Recruit(LocalDateTime.now(), LocalDateTime.now(), "quota","process_description","contact", "web_link");
        User user = new User("testId","testPw" ,3, "test_man","010-1234-1234");
        clubDetailInfoDto = new ClubDetailInfoDto(club, logo, activityImages, recruit, user);
    }

    @AfterEach
    public void afterEach() throws Exception {
    }

    @Test
    public void getClubById_FullData_Success() throws Exception{
        //given
        S3DownloadDto s3DownloadDto = new S3DownloadDto(0L, "logo.png", "testBytes");
        List<S3DownloadDto> s3DownloadDtos = new ArrayList<>();
        s3DownloadDtos.add(new S3DownloadDto(1L, "Ac1.png", "testBytes"));
        s3DownloadDtos.add(new S3DownloadDto(2L, "Ac2.png", "testBytes"));
        s3DownloadDtos.add(new S3DownloadDto(3L, "Ac3.png", "testBytes"));
        s3DownloadDtos.add(new S3DownloadDto(4L, "Ac4.png", "testBytes"));


        given(clubService.getClubDetailInfo(eq(0L))).willReturn(clubDetailInfoDto);
        given(s3Transferer.downloadOne(fileName)).willReturn(s3DownloadDto);
        given(s3Transferer.downloadAll(fileNames)).willReturn(s3DownloadDtos);

        //when
        ResultActions actions = mockMvc.perform(
                get("/club/{clubId}", 0L)
        );
        //then
        actions.andExpect(status().isOk())
                .andDo(
                        document("/club/get/detail",
                                pathParameters(
                                        parameterWithName("clubId").description("동아리 ID")
                                )
                                , responseFields(fieldWithPath("id").type(WireFormat.FieldType.INT64).description("동아리 ID"),
                                        fieldWithPath("name").type(WireFormat.FieldType.STRING).description("동아리 이름"),
                                        fieldWithPath("campus").type(WireFormat.FieldType.STRING).description("캠퍼스"),
                                        fieldWithPath("clubType").type(WireFormat.FieldType.STRING).description("동아리 종류"),
                                        fieldWithPath("college").type(WireFormat.FieldType.STRING).description("소속 대학 종류"),
                                        fieldWithPath("activityType").type(WireFormat.FieldType.STRING).description("동아리 활동 종류"),
                                        fieldWithPath("briefActivityDescription").type(WireFormat.FieldType.STRING).description("동아리 활동 종류 - 세분화"),
                                        fieldWithPath("headLine").type(WireFormat.FieldType.STRING).description("페이지 헤드라인"),
                                        fieldWithPath("establishAt").type(WireFormat.FieldType.STRING).description("설립연도"),
                                        fieldWithPath("roomLocation").type(WireFormat.FieldType.STRING).description("동아리 방 위치"),
                                        fieldWithPath("memberAmount").type(WireFormat.FieldType.INT32).description("활동 인원"),
                                        fieldWithPath("regularMeetingTime").type(WireFormat.FieldType.STRING).description("정규 모임 시간"),
                                        fieldWithPath("mandatoryActivatePeriod").type(WireFormat.FieldType.STRING).description("의무 활동 기간"),
                                        fieldWithPath("clubDescription").type(WireFormat.FieldType.STRING).description("동아리 구체적인 설명"),
                                        fieldWithPath("activityDescription").type(WireFormat.FieldType.STRING).description("활동 구체적인 설명"),
                                        fieldWithPath("webLink1").type(WireFormat.FieldType.STRING).description("웹 주소1"),
                                        fieldWithPath("webLink2").type(WireFormat.FieldType.STRING).description("웹 주소2"),
                                        fieldWithPath("recruitStartAt").type(WireFormat.FieldType.STRING).description("채용 시작 시간"),
                                        fieldWithPath("recruitEndAt").type(WireFormat.FieldType.STRING).description("채용 마감 시간"),
                                        fieldWithPath("recruitQuota").type(WireFormat.FieldType.STRING).description("채용 인원"),
                                        fieldWithPath("recruitProcessDescription").type(WireFormat.FieldType.STRING).description("채용 과정/방법"),
                                        fieldWithPath("recruitContact").type(WireFormat.FieldType.STRING).description("채용 관련 문의"),
                                        fieldWithPath("recruitWebLink").type(WireFormat.FieldType.STRING).description("채용 관련 웹 주소"),
                                        fieldWithPath("presidentName").type(WireFormat.FieldType.STRING).description("회장 이름"),
                                        fieldWithPath("presidentContact").type(WireFormat.FieldType.STRING).description("회장 연락처"),
                                        fieldWithPath("logo.id").type(WireFormat.FieldType.INT64).description("로고 아이디"),
                                        fieldWithPath("logo.fileName").type(WireFormat.FieldType.INT64).description("로고 파일명"),
                                        fieldWithPath("logo.bytes").type(WireFormat.FieldType.INT64).description("로고 바이트"),
                                        fieldWithPath("activityImages[].id").type(WireFormat.FieldType.INT64).description("활동 사진 아이디"),
                                        fieldWithPath("activityImages[].fileName").type(WireFormat.FieldType.INT64).description("활동 사진 파일명"),
                                        fieldWithPath("activityImages[].bytes").type(WireFormat.FieldType.INT64).description("활동 사진 바이트")
                                )
                        )

                );

    }
}
