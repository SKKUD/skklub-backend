package com.skklub.admin.controller.recruit;

import akka.protobuf.WireFormat;
import com.skklub.admin.TestDataRepository;
import com.skklub.admin.controller.AuthValidator;
import com.skklub.admin.controller.RecruitController;
import com.skklub.admin.controller.dto.RecruitRequest;
import com.skklub.admin.domain.Club;
import com.skklub.admin.domain.Recruit;
import com.skklub.admin.exception.deprecated.error.exception.AllTimeRecruitTimeFormattingException;
import com.skklub.admin.exception.deprecated.error.exception.AlreadyRecruitingException;
import com.skklub.admin.exception.deprecated.error.exception.ClubIdMisMatchException;
import com.skklub.admin.exception.deprecated.error.exception.NotRecruitingException;
import com.skklub.admin.service.RecruitService;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.validation.BindException;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.skklub.admin.controller.RestDocsUtils.example;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import(TestDataRepository.class)
@WebMvcTest(controllers = RecruitController.class)
@MockBean(JpaMetamodelMappingContext.class)
@WithMockUser
class RecruitControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private RecruitService recruitService;
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
    public void startRecruit_Default_Success() throws Exception {
        //given
        Long clubId = 0L;
        Club club = testDataRepository.getClubs().get(clubId.intValue());
        RecruitRequest recruitRequest = readyRecruitReqFullTime();
        Recruit recruit = recruitRequest.toEntity();
        given(recruitService.startRecruit(clubId, recruit)).willReturn(Optional.of(club.getName()));
        doNothing().when(authValidator).validateUpdatingClub(clubId);

        //when
        ResultActions actions = mockMvc.perform(
                post("/recruit/{clubId}", clubId)
                        .with(csrf())
                        .queryParam("recruitStartAt", Optional.ofNullable(recruitRequest.getRecruitStartAt()).map(Object::toString).orElse(""))
                        .queryParam("recruitEndAt", Optional.ofNullable(recruitRequest.getRecruitEndAt()).map(Object::toString).orElse(""))
                        .queryParam("recruitQuota", recruitRequest.getRecruitQuota())
                        .queryParam("recruitProcessDescription", recruitRequest.getRecruitProcessDescription())
                        .queryParam("recruitContact", recruitRequest.getRecruitContact())
                        .queryParam("recruitWebLink", recruitRequest.getRecruitWebLink())
        );

        //then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clubId))
                .andExpect(jsonPath("$.name").value(club.getName()))
                .andDo(
                        document("recruit/create",
                                pathParameters(
                                        parameterWithName("clubId").description("동아리 ID").attributes(example("1"))
                                ),
                                queryParameters(
                                        parameterWithName("recruitStartAt").description("모집 시작일").attributes(example("yyyy-MM-ddTHH:mm(T는 날짜랑 시간 구분용 문자)")).optional(),
                                        parameterWithName("recruitEndAt").description("모집 종료일").attributes(example("2012-06-02T14:04(상시모집은 시작일 종료일 null or No Field)")).optional(),
                                        parameterWithName("recruitQuota").description("모집 정원 - String value").attributes(example("00명 || 최대한 많이 뽑을 예정")),
                                        parameterWithName("recruitProcessDescription").description("모집 방식").attributes(example("1. 어쩌구 2. 어쩌구 AnyString")),
                                        parameterWithName("recruitContact").description("모집 문의처").attributes(example("010 - 1234 - 1234 || 인스타 아이디")).optional(),
                                        parameterWithName("recruitWebLink").description("모집 링크").attributes(example("www.xxx.com || or any String")).optional()
                                ),
                                responseFields(
                                        fieldWithPath("id").type(WireFormat.FieldType.STRING).description("동아리 ID").attributes(example("0")),
                                        fieldWithPath("name").type(WireFormat.FieldType.STRING).description("동아리 이름").attributes(example("클럽 SKKULOL"))
                                )
                        )
                );

    }

    @Test
    public void startRecruit_IllegalClubId_UnMatchClubException() throws Exception {
        //given
        Long clubId = -1L;
        Long recruitId = 0L;
        RecruitRequest recruitRequest = readyRecruitReqFullTime();
        Recruit recruit = recruitRequest.toEntity();
        given(recruitService.startRecruit(clubId, recruit)).willReturn(Optional.empty());
        doNothing().when(authValidator).validateUpdatingClub(clubId);

        //when
        MvcResult badClubIdResult = mockMvc.perform(
                post("/recruit/{clubId}", clubId)
                        .queryParam("recruitStartAt", Optional.ofNullable(recruitRequest.getRecruitStartAt()).map(Object::toString).orElse(""))
                        .queryParam("recruitEndAt", Optional.ofNullable(recruitRequest.getRecruitEndAt()).map(Object::toString).orElse(""))
                        .queryParam("recruitQuota", recruitRequest.getRecruitQuota())
                        .queryParam("recruitProcessDescription", recruitRequest.getRecruitProcessDescription())
                        .queryParam("recruitContact", recruitRequest.getRecruitContact())
                        .queryParam("recruitWebLink", recruitRequest.getRecruitWebLink())
                        .with(csrf())
        ).andExpect(status().isBadRequest()).andReturn();

        //then
        Assertions.assertThat(badClubIdResult.getResolvedException()).isExactlyInstanceOf(ClubIdMisMatchException.class);
    }

    @Test
    public void startRecruit_ClubRecruitNotNull_AlreadyRecruitingException() throws Exception {
        //given
        Long clubId = 0L;
        Long recruitId = 0L;
        RecruitRequest recruitRequest = readyRecruitReqFullTime();
        Recruit recruit = recruitRequest.toEntity();
        given(recruitService.startRecruit(clubId, recruit)).willThrow(AlreadyRecruitingException.class);
        doNothing().when(authValidator).validateUpdatingClub(clubId);

        //when
        MvcResult doubleRecruitResult = mockMvc.perform(
                        post("/recruit/{clubId}", clubId)
                                .queryParam("recruitStartAt", Optional.ofNullable(recruitRequest.getRecruitStartAt()).map(Object::toString).orElse(""))
                                .queryParam("recruitEndAt", Optional.ofNullable(recruitRequest.getRecruitEndAt()).map(Object::toString).orElse(""))
                                .queryParam("recruitQuota", recruitRequest.getRecruitQuota())
                                .queryParam("recruitProcessDescription", recruitRequest.getRecruitProcessDescription())
                                .queryParam("recruitContact", recruitRequest.getRecruitContact())
                                .queryParam("recruitWebLink", recruitRequest.getRecruitWebLink())
                                .with(csrf())
                ).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorDetail.reasonMessage").value("이미 모집 정보가 등록된 club입니다"))
                .andReturn();

        //then
        Assertions.assertThat(doubleRecruitResult.getResolvedException()).isExactlyInstanceOf(AlreadyRecruitingException.class);

    }

    @Test
    public void startRecruit_BothTimeNullOrNotNUll_Success() throws Exception {
        //given
        Long clubId = 0L;
        String clubName = "test";
        RecruitRequest fullTime = readyRecruitReqFullTime();
        RecruitRequest bothNull = readyRecruitReqNoTime();
        given(recruitService.startRecruit(eq(clubId), any(Recruit.class))).willReturn(Optional.of(clubName));
        doNothing().when(authValidator).validateUpdatingClub(clubId);

        //when
        mockMvc.perform(
                post("/recruit/{clubId}", clubId)
                        .queryParam("recruitStartAt", Optional.ofNullable(fullTime.getRecruitStartAt()).map(Object::toString).orElse(""))
                        .queryParam("recruitEndAt", Optional.ofNullable(fullTime.getRecruitEndAt()).map(Object::toString).orElse(""))
                        .queryParam("recruitQuota", fullTime.getRecruitQuota())
                        .queryParam("recruitProcessDescription", fullTime.getRecruitProcessDescription())
                        .queryParam("recruitContact", fullTime.getRecruitContact())
                        .queryParam("recruitWebLink", fullTime.getRecruitWebLink())
                        .with(csrf())
        ).andExpect(status().isOk());
        mockMvc.perform(
                post("/recruit/{clubId}", clubId)
                        .queryParam("recruitStartAt", Optional.ofNullable(bothNull.getRecruitStartAt()).map(Object::toString).orElse(""))
                        .queryParam("recruitEndAt", Optional.ofNullable(bothNull.getRecruitEndAt()).map(Object::toString).orElse(""))
                        .queryParam("recruitQuota", bothNull.getRecruitQuota())
                        .queryParam("recruitProcessDescription", bothNull.getRecruitProcessDescription())
                        .queryParam("recruitContact", bothNull.getRecruitContact())
                        .queryParam("recruitWebLink", bothNull.getRecruitWebLink())
                        .with(csrf())
        ).andExpect(status().isOk());
        //then

    }

    @Test
    public void startRecruit_OnlyOneTimeNull_AllTimeRecruitTimeFormattingException() throws Exception {
        //given
        Long clubId = 0L;
        String clubName = "test";
        RecruitRequest endTimeNull = readyRecruitReqFullTime();
        endTimeNull.setRecruitEndAt(null);

        RecruitRequest startTimeNull = readyRecruitReqFullTime();
        startTimeNull.setRecruitStartAt(null);
        given(recruitService.startRecruit(eq(clubId), any(Recruit.class))).willReturn(Optional.of(clubName));
        doNothing().when(authValidator).validateUpdatingClub(clubId);

        //when
        MvcResult startNullResult = mockMvc.perform(
                post("/recruit/{clubId}", clubId)
                        .queryParam("recruitStartAt", Optional.ofNullable(startTimeNull.getRecruitStartAt()).map(Object::toString).orElse(""))
                        .queryParam("recruitEndAt", Optional.ofNullable(startTimeNull.getRecruitEndAt()).map(Object::toString).orElse(""))
                        .queryParam("recruitQuota", startTimeNull.getRecruitQuota())
                        .queryParam("recruitProcessDescription", startTimeNull.getRecruitProcessDescription())
                        .queryParam("recruitContact", startTimeNull.getRecruitContact())
                        .queryParam("recruitWebLink", startTimeNull.getRecruitWebLink())
                        .with(csrf())
        ).andExpect(status().isBadRequest()).andReturn();
        MvcResult endNullResult = mockMvc.perform(
                post("/recruit/{clubId}", clubId)
                        .queryParam("recruitStartAt", Optional.ofNullable(endTimeNull.getRecruitStartAt()).map(Object::toString).orElse(""))
                        .queryParam("recruitEndAt", Optional.ofNullable(endTimeNull.getRecruitEndAt()).map(Object::toString).orElse(""))
                        .queryParam("recruitQuota", endTimeNull.getRecruitQuota())
                        .queryParam("recruitProcessDescription", endTimeNull.getRecruitProcessDescription())
                        .queryParam("recruitContact", endTimeNull.getRecruitContact())
                        .queryParam("recruitWebLink", endTimeNull.getRecruitWebLink())
                        .with(csrf())
        ).andExpect(status().isBadRequest()).andReturn();

        //then
        Assertions.assertThat(startNullResult.getResolvedException()).isExactlyInstanceOf(AllTimeRecruitTimeFormattingException.class);
        Assertions.assertThat(endNullResult.getResolvedException()).isExactlyInstanceOf(AllTimeRecruitTimeFormattingException.class);

    }

    @Test
    public void updateRecruit_Default_Success() throws Exception {
        //given
        Long clubId = 0L;
        RecruitRequest recruitRequest = readyRecruitReqFullTime();
        Recruit recruit = recruitRequest.toEntity();
        given(recruitService.updateRecruit(clubId, recruit)).willReturn(Optional.of(clubId));
        doNothing().when(authValidator).validateUpdatingClub(clubId);

        //when
        ResultActions actions = mockMvc.perform(
                patch("/recruit/{clubId}", clubId)
                        .queryParam("recruitStartAt", Optional.ofNullable(recruitRequest.getRecruitStartAt()).map(Object::toString).orElse(""))
                        .queryParam("recruitEndAt", Optional.ofNullable(recruitRequest.getRecruitEndAt()).map(Object::toString).orElse(""))
                        .queryParam("recruitQuota", recruitRequest.getRecruitQuota())
                        .queryParam("recruitProcessDescription", recruitRequest.getRecruitProcessDescription())
                        .queryParam("recruitContact", recruitRequest.getRecruitContact())
                        .queryParam("recruitWebLink", recruitRequest.getRecruitWebLink())
                        .with(csrf())
        );

        //then
        actions.andExpect(status().isOk())
                .andExpect(content().json(clubId.toString()))
                .andDo(
                        document("recruit/update",
                                pathParameters(
                                        parameterWithName("clubId").description("모집중인 동아리 ID").attributes(example("1"))
                                ),
                                queryParameters(
                                        parameterWithName("recruitStartAt").description("모집 시작일").attributes(example("yyyy-MM-ddTHH:mm(T는 날짜랑 시간 구분용 문자)")).optional(),
                                        parameterWithName("recruitEndAt").description("모집 종료일").attributes(example("2012-06-02T14:04(상시모집은 시작일 종료일 null or No Field)")).optional(),
                                        parameterWithName("recruitQuota").description("모집 정원 - String value").attributes(example("00명 || 최대한 많이 뽑을 예정")),
                                        parameterWithName("recruitProcessDescription").description("모집 방식").attributes(example("1. 어쩌구 2. 어쩌구 AnyString")),
                                        parameterWithName("recruitContact").description("모집 문의처").attributes(example("010 - 1234 - 1234 || 인스타 아이디")).optional(),
                                        parameterWithName("recruitWebLink").description("모집 링크").attributes(example("www.xxx.com || or any String")).optional()
                                )
                        )
                );

    }

    @Test
    public void updateRecruit_NullAtNotNull_BindException() throws Exception {
        //given
        Long clubId = 0L;
        RecruitRequest nullAtQuota = readyRecruitReqFullTime();
        nullAtQuota.setRecruitQuota(null);

        RecruitRequest blankAtDescription = readyRecruitReqFullTime();
        blankAtDescription.setRecruitProcessDescription("  ");
        doNothing().when(authValidator).validateUpdatingClub(clubId);

        //when
        MvcResult nullQuotaResult = mockMvc.perform(
                patch("/recruit/{clubId}", clubId)
                        .queryParam("recruitStartAt", Optional.ofNullable(nullAtQuota.getRecruitStartAt()).map(Object::toString).orElse(""))
                        .queryParam("recruitEndAt", Optional.ofNullable(nullAtQuota.getRecruitEndAt()).map(Object::toString).orElse(""))
                        .queryParam("recruitQuota", nullAtQuota.getRecruitQuota())
                        .queryParam("recruitProcessDescription", nullAtQuota.getRecruitProcessDescription())
                        .queryParam("recruitContact", nullAtQuota.getRecruitContact())
                        .queryParam("recruitWebLink", nullAtQuota.getRecruitWebLink())
                        .with(csrf())
        ).andExpect(status().isBadRequest()).andReturn();

        MvcResult blankDescriptionResult = mockMvc.perform(
                patch("/recruit/{clubId}", clubId)
                        .queryParam("recruitStartAt", Optional.ofNullable(blankAtDescription.getRecruitStartAt()).map(Object::toString).orElse(""))
                        .queryParam("recruitEndAt", Optional.ofNullable(blankAtDescription.getRecruitEndAt()).map(Object::toString).orElse(""))
                        .queryParam("recruitQuota", blankAtDescription.getRecruitQuota())
                        .queryParam("recruitProcessDescription", blankAtDescription.getRecruitProcessDescription())
                        .queryParam("recruitContact", blankAtDescription.getRecruitContact())
                        .queryParam("recruitWebLink", blankAtDescription.getRecruitWebLink())
                        .with(csrf())
        ).andExpect(status().isBadRequest()).andReturn();

        //then
        Assertions.assertThat(blankDescriptionResult.getResolvedException()).isExactlyInstanceOf(BindException.class);
        Assertions.assertThat(nullQuotaResult.getResolvedException()).isExactlyInstanceOf(BindException.class);

    }

    @Test
    public void updateRecruit_BothTimeNullOrNotNUll_Success() throws Exception {
        //given
        Long clubId = 0L;
        RecruitRequest fullTime = readyRecruitReqFullTime();
        RecruitRequest bothNull = readyRecruitReqNoTime();
        given(recruitService.updateRecruit(eq(clubId), any(Recruit.class))).willReturn(Optional.of(clubId));
        doNothing().when(authValidator).validateUpdatingClub(clubId);

        //when
        mockMvc.perform(
                patch("/recruit/{clubId}", clubId)
                        .queryParam("recruitStartAt", Optional.ofNullable(fullTime.getRecruitStartAt()).map(Object::toString).orElse(""))
                        .queryParam("recruitEndAt", Optional.ofNullable(fullTime.getRecruitEndAt()).map(Object::toString).orElse(""))
                        .queryParam("recruitQuota", fullTime.getRecruitQuota())
                        .queryParam("recruitProcessDescription", fullTime.getRecruitProcessDescription())
                        .queryParam("recruitContact", fullTime.getRecruitContact())
                        .queryParam("recruitWebLink", fullTime.getRecruitWebLink())
                        .with(csrf())
        ).andExpect(status().isOk());
        mockMvc.perform(
                patch("/recruit/{clubId}", clubId)
                        .queryParam("recruitStartAt", Optional.ofNullable(bothNull.getRecruitStartAt()).map(Object::toString).orElse(""))
                        .queryParam("recruitEndAt", Optional.ofNullable(bothNull.getRecruitEndAt()).map(Object::toString).orElse(""))
                        .queryParam("recruitQuota", bothNull.getRecruitQuota())
                        .queryParam("recruitProcessDescription", bothNull.getRecruitProcessDescription())
                        .queryParam("recruitContact", bothNull.getRecruitContact())
                        .queryParam("recruitWebLink", bothNull.getRecruitWebLink())
                        .with(csrf())
        ).andExpect(status().isOk());
        //then

    }

    @Test
    public void updateRecruit_OnlyOneTimeNull_AllTimeRecruitTimeFormattingException() throws Exception {
        //given
        Long clubId = 0L;
        RecruitRequest endTimeNull = readyRecruitReqFullTime();
        endTimeNull.setRecruitEndAt(null);

        RecruitRequest startTimeNull = readyRecruitReqFullTime();
        startTimeNull.setRecruitStartAt(null);
        given(recruitService.updateRecruit(eq(clubId), any(Recruit.class))).willReturn(Optional.of(clubId));
        doNothing().when(authValidator).validateUpdatingClub(clubId);

        //when
        MvcResult startNullResult = mockMvc.perform(
                patch("/recruit/{clubId}", clubId)
                        .queryParam("recruitEndAt", Optional.ofNullable(startTimeNull.getRecruitEndAt()).map(Object::toString).orElse(""))
                        .queryParam("recruitQuota", startTimeNull.getRecruitQuota())
                        .queryParam("recruitProcessDescription", startTimeNull.getRecruitProcessDescription())
                        .queryParam("recruitContact", startTimeNull.getRecruitContact())
                        .queryParam("recruitWebLink", startTimeNull.getRecruitWebLink())
                        .with(csrf())
        ).andExpect(status().isBadRequest()).andReturn();
        MvcResult endNullResult = mockMvc.perform(
                patch("/recruit/{clubId}", clubId)
                        .queryParam("recruitStartAt", Optional.ofNullable(endTimeNull.getRecruitStartAt()).map(Object::toString).orElse(""))
                        .queryParam("recruitQuota", endTimeNull.getRecruitQuota())
                        .queryParam("recruitProcessDescription", endTimeNull.getRecruitProcessDescription())
                        .queryParam("recruitContact", endTimeNull.getRecruitContact())
                        .queryParam("recruitWebLink", endTimeNull.getRecruitWebLink())
                        .with(csrf())
        ).andExpect(status().isBadRequest()).andReturn();

        //then
        Assertions.assertThat(startNullResult.getResolvedException()).isExactlyInstanceOf(AllTimeRecruitTimeFormattingException.class);
        Assertions.assertThat(endNullResult.getResolvedException()).isExactlyInstanceOf(AllTimeRecruitTimeFormattingException.class);

    }

    @Test
    public void endRecruit_Default_Success() throws Exception {
        //given
        Long clubId = 0L;
        doNothing().when(recruitService).endRecruit(clubId);
        doNothing().when(authValidator).validateUpdatingClub(clubId);

        //when
        ResultActions actions = mockMvc.perform(
                delete("/recruit/{clubId}", clubId)
                        .with(csrf())
        );

        //then
        actions.andExpect(status().isOk())
                .andExpect(content().json(clubId.toString()))
                .andDo(
                        document("recruit/delete",
                                pathParameters(
                                        parameterWithName("clubId").description("동아리 ID").attributes(example("1"))
                                )
                        ));
    }

    @Test
    public void endRecruit_IllegalClubId_ClubIdMisMatchException() throws Exception {
        //given
        Long clubId = -1L;
        doThrow(ClubIdMisMatchException.class).when(recruitService).endRecruit(clubId);
        doNothing().when(authValidator).validateUpdatingClub(clubId);

        //when
        MvcResult result = mockMvc.perform(
                delete("/recruit/{clubId}", clubId)
                        .with(csrf())
        ).andExpect(status().isBadRequest()).andReturn();

        //then
        Assertions.assertThat(result.getResolvedException()).isExactlyInstanceOf(ClubIdMisMatchException.class);

    }

    @Test
    public void endRecruit_OnRecruitNull_NotRecruitingException() throws Exception {
        //given
        Long clubId = -1L;
        doThrow(NotRecruitingException.class).when(recruitService).endRecruit(clubId);
        doNothing().when(authValidator).validateUpdatingClub(clubId);

        //when
        MvcResult result = mockMvc.perform(
                delete("/recruit/{clubId}", clubId)
                        .with(csrf())
        ).andExpect(status().isBadRequest()).andReturn();

        //then
        Assertions.assertThat(result.getResolvedException()).isExactlyInstanceOf(NotRecruitingException.class);

    }

    private RecruitRequest readyRecruitReqFullTime() {
        return RecruitRequest.builder()
                .recruitStartAt(LocalDateTime.now())
                .recruitEndAt(LocalDateTime.now())
                .recruitQuota("00명 || 최대한 많이 뽑을 예정")
                .recruitProcessDescription("1. 어쩌구 2. 어쩌구 AnyString")
                .recruitContact("010 - 1234 - 1234 || 인스타 아이디")
                .recruitWebLink("www.xxx.com || or any String")
                .build();
    }

    private RecruitRequest readyRecruitReqNoTime() {
        return RecruitRequest.builder()
                .recruitQuota("00명 || 최대한 많이 뽑을 예정")
                .recruitProcessDescription("1. 어쩌구 2. 어쩌구 AnyString")
                .recruitContact("010 - 1234 - 1234 || 인스타 아이디")
                .recruitWebLink("www.xxx.com || or any String")
                .build();
    }
}