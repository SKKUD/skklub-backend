package com.skklub.admin.controller.recruit;

import akka.protobuf.WireFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skklub.admin.controller.ClubTestDataRepository;
import com.skklub.admin.controller.RecruitController;
import com.skklub.admin.controller.S3Transferer;
import com.skklub.admin.controller.dto.RecruitDto;
import com.skklub.admin.controller.error.exception.AllTimeRecruitTimeFormattingException;
import com.skklub.admin.controller.error.exception.AlreadyRecruitingException;
import com.skklub.admin.controller.error.exception.ClubIdMisMatchException;
import com.skklub.admin.domain.Club;
import com.skklub.admin.domain.Recruit;
import com.skklub.admin.service.ClubService;
import com.skklub.admin.service.RecruitService;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Optional;

import static com.skklub.admin.controller.RestDocsUtils.example;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import(ClubTestDataRepository.class)
@WebMvcTest(controllers = RecruitController.class)
@MockBean(JpaMetamodelMappingContext.class)
@WithMockUser
class RecruitControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private RecruitService recruitService;
    @Autowired
    private ClubTestDataRepository clubTestDataRepository;
    @Autowired
    private ObjectMapper objectMapper;


    @Test
    public void startRecruit_Default_Success() throws Exception{
        //given
        Long clubId = 0L;
        Long recruitId = 0L;
        Club club = clubTestDataRepository.getClubs().get(clubId.intValue());
        RecruitDto recruitDto = new RecruitDto(clubTestDataRepository.getRecruits().get(recruitId.intValue()));
        Recruit recruit = recruitDto.toEntity();
        String recruitDtoJson = objectMapper.writeValueAsString(recruitDto);
        given(recruitService.startRecruit(clubId, recruit)).willReturn(Optional.of(club.getName()));


        //when
        ResultActions actions = mockMvc.perform(
                post("/recruit/{clubId}", clubId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(recruitDtoJson)
                        .with(csrf())
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
                                requestFields(
                                        fieldWithPath("recruitStartAt").description("모집 시작일").attributes(example("yyyy-MM-ddTHH:mm(T는 날짜랑 시간 구분용 문자)")).optional(),
                                        fieldWithPath("recruitEndAt").description("모집 종료일").attributes(example("2012-06-02T14:04(상시모집은 시작일 종료일 null or No Field)")).optional(),
                                        fieldWithPath("recruitQuota").description("모집 정원 - String value").attributes(example("00명 || 최대한 많이 뽑을 예정")),
                                        fieldWithPath("recruitProcessDescription").description("모집 방식").attributes(example("1. 어쩌구 2. 어쩌구 AnyString")),
                                        fieldWithPath("recruitContact").description("모집 문의처").attributes(example("010 - 1234 - 1234 || 인스타 아이디")).optional(),
                                        fieldWithPath("recruitWebLink").description("모집 링크").attributes(example("www.xxx.com || or any String")).optional()
                                ),
                                responseFields(
                                        fieldWithPath("id").type(WireFormat.FieldType.STRING).description("동아리 ID").attributes(example("0")),
                                        fieldWithPath("name").type(WireFormat.FieldType.STRING).description("동아리 이름").attributes(example("클럽 SKKULOL"))
                                )
                        )
                );

    }

    @Test
    public void startRecruit_IllegalClubId_UnMatchClubException() throws Exception{
        //given
        Long clubId = -1L;
        Long recruitId = 0L;
        RecruitDto recruitDto = new RecruitDto(clubTestDataRepository.getRecruits().get(recruitId.intValue()));
        Recruit recruit = recruitDto.toEntity();
        String recruitDtoJson = objectMapper.writeValueAsString(recruitDto);
        given(recruitService.startRecruit(clubId, recruit)).willReturn(Optional.empty());

        //when
        MvcResult badClubIdResult = mockMvc.perform(
                post("/recruit/{clubId}", clubId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(recruitDtoJson)
                        .with(csrf())
        ).andReturn();

        //then
        Assertions.assertThat(badClubIdResult.getResolvedException()).isExactlyInstanceOf(ClubIdMisMatchException.class);
    }

    @Test
    public void startRecruit_ClubRecruitNotNull_AlreadyRecruitingException() throws Exception{
        //given
        Long clubId = 0L;
        Long recruitId = 0L;
        RecruitDto recruitDto = new RecruitDto(clubTestDataRepository.getRecruits().get(recruitId.intValue()));
        Recruit recruit = recruitDto.toEntity();
        String recruitDtoJson = objectMapper.writeValueAsString(recruitDto);
        given(recruitService.startRecruit(clubId, recruit)).willThrow(AlreadyRecruitingException.class);

        //when
        MvcResult doubleRecruitResult = mockMvc.perform(
                        post("/recruit/{clubId}", clubId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(recruitDtoJson)
                                .with(csrf())
                ).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorDetail.reasonMessage").value("이미 모집 정보가 등록된 club입니다"))
                .andReturn();

        //then
        Assertions.assertThat(doubleRecruitResult.getResolvedException()).isExactlyInstanceOf(AlreadyRecruitingException.class);

    }

    @Test
    public void startRecruit_BothTimeNullOrNotNUll_Success() throws Exception{
        //given
        Long clubId = 0L;
        String clubName = "test";
        Long recruitId = 0L;
        RecruitDto recruitDto1 = new RecruitDto(clubTestDataRepository.getRecruits().get(recruitId.intValue()));
        String fullTime = objectMapper.writeValueAsString(recruitDto1);
        RecruitDto recruitDto2 = new RecruitDto(clubTestDataRepository.getRecruits().get(recruitId.intValue()));
        recruitDto2.setRecruitStartAt(null);
        recruitDto2.setRecruitEndAt(null);
        String bothNull = objectMapper.writeValueAsString(recruitDto2);
        given(recruitService.startRecruit(eq(clubId), any(Recruit.class))).willReturn(Optional.of(clubName));

        //when
        mockMvc.perform(
                post("/recruit/{clubId}", clubId)
                        .content(fullTime)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
        ).andExpect(status().isOk());
        mockMvc.perform(
                post("/recruit/{clubId}", clubId)
                        .content(bothNull)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
        ).andExpect(status().isOk());
        //then

     }

     @Test
     public void startRecruit_OnlyOneTimeNull_AllTimeRecruitTimeFormattingException() throws Exception{
         //given
         Long clubId = 0L;
         String clubName = "test";
         Long recruitId = 0L;
         RecruitDto recruitDto1 = new RecruitDto(clubTestDataRepository.getRecruits().get(recruitId.intValue()));
         recruitDto1.setRecruitEndAt(null);
         String endTimeNull = objectMapper.writeValueAsString(recruitDto1);

         RecruitDto recruitDto2 = new RecruitDto(clubTestDataRepository.getRecruits().get(recruitId.intValue()));
         recruitDto2.setRecruitStartAt(null);
         String startTimeNull = objectMapper.writeValueAsString(recruitDto2);
         given(recruitService.startRecruit(eq(clubId), any(Recruit.class))).willReturn(Optional.of(clubName));

         //when
         MvcResult startNullResult = mockMvc.perform(
                 post("/recruit/{clubId}", clubId)
                         .content(startTimeNull)
                         .contentType(MediaType.APPLICATION_JSON)
                         .with(csrf())
         ).andReturn();
         MvcResult endNullResult = mockMvc.perform(
                 post("/recruit/{clubId}", clubId)
                         .content(endTimeNull)
                         .contentType(MediaType.APPLICATION_JSON)
                         .with(csrf())
         ).andReturn();

         //then
         Assertions.assertThat(startNullResult.getResolvedException()).isExactlyInstanceOf(AllTimeRecruitTimeFormattingException.class);
         Assertions.assertThat(endNullResult.getResolvedException()).isExactlyInstanceOf(AllTimeRecruitTimeFormattingException.class);

      }

}