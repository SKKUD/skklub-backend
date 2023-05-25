package com.skklub.admin.controller.club;


import akka.protobuf.WireFormat;
import com.skklub.admin.controller.ApiDocumentUtils;
import com.skklub.admin.controller.ClubController;
import com.skklub.admin.controller.S3Transferer;
import com.skklub.admin.controller.dto.ClubCreateRequestDTO;
import com.skklub.admin.service.ClubService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@WebMvcTest(controllers = ClubController.class)
@MockBean(JpaMetamodelMappingContext.class)
class ClubControllerUpdateTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ClubService clubService;
    @MockBean
    private S3Transferer s3Transferer;

    @Test
    public void updateClub_Default_Success() throws Exception{
        //given
        long clubId = 0L;
        String clubName = "Test Club";
        given(clubService.updateClub(eq(clubId), any(ClubCreateRequestDTO.class))).willReturn(Optional.of(clubName));

        //when
        ResultActions actions = mockMvc.perform(
                patch("/club/{clubId}", clubId)
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
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clubId))
                .andExpect(jsonPath("$.name").value(clubName))
                .andDo(
                        document("/club/update",
                                pathParameters(
                                        parameterWithName("clubId").description("대상 동아리 ID")
                                ),
                                queryParameters(
                                        parameterWithName("clubName").description("동아리 이름"),
                                        parameterWithName("campus").attributes(ApiDocumentUtils.getCampusFormat()).description("분류 - 캠퍼스"),
                                        parameterWithName("clubType").attributes(ApiDocumentUtils.getClubTypeFormat()).description("분류 - 동아리 종류"),
                                        parameterWithName("belongs").attributes(ApiDocumentUtils.getBelongsFormat()).description("분류 - 동아리 분과"),
                                        parameterWithName("briefActivityDescription").description(" 분류 - 활동 설명"),
                                        parameterWithName("activityDescription").description("자세한 활동 설명"),
                                        parameterWithName("clubDescription").description("자세한 동아리 설명"),
                                        parameterWithName("establishDate").description("설립 연도").optional(),
                                        parameterWithName("headLine").description("한줄 소개").optional(),
                                        parameterWithName("mandatoryActivatePeriod").description("의무 활동 기간").optional(),
                                        parameterWithName("memberAmount").description("동아리 인원").optional(),
                                        parameterWithName("regularMeetingTime").description("정규 모임 시간").optional(),
                                        parameterWithName("roomLocation").description("동아리 방 위치").optional(),
                                        parameterWithName("webLink1").description("관련 사이트 주소 1").optional(),
                                        parameterWithName("webLink2").description("관련 사이트 주소 2").optional()
                                ),
                                responseFields(
                                        fieldWithPath("id").type(WireFormat.FieldType.INT64).description("수정된 동아리 ID")
                                        , fieldWithPath("name").type(WireFormat.FieldType.STRING).description("수정된 동아리 이름")
                                )
                        )
                );

     }
}