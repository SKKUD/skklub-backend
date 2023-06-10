package com.skklub.admin.controller.club;

import akka.protobuf.WireFormat;
import com.skklub.admin.controller.ClubController;
import com.skklub.admin.controller.ClubTestDataRepository;
import com.skklub.admin.controller.S3Transferer;
import com.skklub.admin.controller.dto.S3DownloadDto;
import com.skklub.admin.domain.Club;
import com.skklub.admin.domain.enums.Campus;
import com.skklub.admin.domain.enums.ClubType;
import com.skklub.admin.service.dto.ClubPrevDTO;
import com.skklub.admin.service.ClubService;
import com.skklub.admin.service.dto.ClubDetailInfoDto;
import com.skklub.admin.service.dto.FileNames;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.skklub.admin.controller.RestDocsUtils.example;
import static java.time.LocalTime.now;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@ContextConfiguration(classes = ClubTestDataRepository.class)
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
    private ClubTestDataRepository clubTestDataRepository;

    @Test
    public void getClubById_Default_Success() throws Exception{
        //given
        int clubId = 0;
        S3DownloadDto s3DownloadDto = clubTestDataRepository.getLogoS3DownloadDto(clubId);
        List<S3DownloadDto> s3DownloadDtos = clubTestDataRepository.getActivityImgS3DownloadDtos(clubId);


        ClubDetailInfoDto clubDetailInfoDto = new ClubDetailInfoDto(clubTestDataRepository.getClubs().get(clubId));
        given(clubService.getClubDetailInfoById(eq(0L))).willReturn(Optional.of(clubDetailInfoDto));
        given(s3Transferer.downloadOne(clubTestDataRepository.getLogoFileName(clubId))).willReturn(s3DownloadDto);
        given(s3Transferer.downloadAll(clubTestDataRepository.getActivityImgFileNames(clubId))).willReturn(s3DownloadDtos);

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
                                        fieldWithPath("belongs").type(WireFormat.FieldType.STRING).description("동아리 분과"),
                                        fieldWithPath("briefActivityDescription").type(WireFormat.FieldType.STRING).description("동아리 활동 종류 - 세분화"),
                                        fieldWithPath("headLine").type(WireFormat.FieldType.STRING).description("한줄 소개"),
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

    @Test
    public void getClubPrev_SelectToEnd_Success() throws Exception{
        //given
        Page<ClubPrevDTO> page = Page.empty();
        PageRequest request = PageRequest.of(2, 3, Sort.Direction.ASC, "name");
//        given(clubService.getClubPrevsByCategories(Campus.명륜, ClubType.중앙동아리, "취미교양", request)).willReturn(page);

        //when
        ResultActions actions = mockMvc.perform(
                get("/club/prev/{campus}/{clubType}/{belongs}", "명륜", "중앙동아리", "취미교양")
                        .queryParam("size", "3")
                        .queryParam("page", "2")
                        .queryParam("sortBy", "name-asc")
        );

        //then
//        actions.andExpect(status().isOk())
//                .andDo(
//                        document("/club/Prev",
//                                pathParameters(
//                                        parameterWithName("").description(),
//                                        parameterWithName().description(),
//                                        parameterWithName().description()
//                                ),
//                                queryParameters(
//                                        parameterWithName().description(),
//                                        parameterWithName().description(),
//                                        parameterWithName().description()
//                                ),
//                                responseFields(
//
//                                )
//                        )
//                );

     }

     @Test
     public void getClubPrev_FullCategory_Success() throws Exception{
         //given
         Campus campus = Campus.명륜;
         ClubType clubType = ClubType.중앙동아리;
         String belongs = "취미교양";
         int clubCnt = clubTestDataRepository.getClubCnt();
         PageRequest request = PageRequest.of(0, clubCnt, Sort.Direction.ASC, "name");
         List<ClubPrevDTO> clubPrevs = clubTestDataRepository.getClubPrevDTOs();
         PageImpl<ClubPrevDTO> clubPrevDTOPage = new PageImpl<>(clubPrevs, request, clubPrevs.size());

         given(clubService.getClubPrevsByCategories(campus, clubType, belongs, request)).willReturn(clubPrevDTOPage);
         clubPrevs.stream()
                 .forEach(prevs -> given(s3Transferer.downloadOne(prevs.getLogo())).willReturn(clubTestDataRepository.getLogoS3DownloadDto((int) (long) prevs.getId())));
         //when
         ResultActions actions = mockMvc.perform(
                 get("/club/prev/{campus}/{clubType}/{belongs}", campus.toString(), clubType.toString(), belongs)
                         .queryParam("size", "5")
                         .queryParam("page", "0")
                         .queryParam("sortBy", "name,ASC")
         );

         //then
         for(int i = 0; i < clubCnt; i++) {
             actions = buildPageableResponse(actions.andExpect(status().isOk())
                     .andExpect(jsonPath("$.size").value(5))
                     .andExpect(jsonPath("$.totalPages").value(1))
                     .andExpect(jsonPath("$.pageable.sort.sorted").value("true")), i);
         }
         actions.andDo(
                 document("/club/get/prevs/category",
                         pathParameters(
                                 parameterWithName("campus").description("분류 - 캠퍼스").attributes(example("link:common/campus-type.html[캠퍼스 종류,role=\"popup\"]")),
                                 parameterWithName("clubType").description("분류 - 동아리 종류").attributes(example("link:common/club-type-null.html[동아리 종류,role=\"popup\"]")),
                                 parameterWithName("belongs").description("분류 - 동아리 분과").attributes(example("link:common/belongs-null.html[분과 종류,role=\"popup\"]"))
                         ),
                         queryParameters(
                                 parameterWithName("size").optional().description("페이지 정보 - 한 페이지 크기"),
                                 parameterWithName("page").optional().description("페이지 정보 - 요청 페이지 번호(시작 0)"),
                                 parameterWithName("sortBy").optional().description("페이지 정보 - 정렬").attributes(example("link:common/sorting.html[정렬,role=\"popup\"]"))
                         ),
                         responseFields(
                                 fieldWithPath("content[].id").type(WireFormat.FieldType.INT64).description("동아리 아이디"),
                                 fieldWithPath("content[].name").type(WireFormat.FieldType.STRING).description("동아리 이름"),
                                 fieldWithPath("content[].belongs").type(WireFormat.FieldType.STRING).description("분류 - 활동 설명"),
                                 fieldWithPath("content[].briefActivityDescription").type(WireFormat.FieldType.STRING).description("클럽 "),
                                 fieldWithPath("content[].logo.id").type(WireFormat.FieldType.INT64).description("로고 아이디"),
                                 fieldWithPath("content[].logo.fileName").type(WireFormat.FieldType.STRING).description("로고 원본 파일명"),
                                 fieldWithPath("content[].logo.bytes").type(WireFormat.FieldType.BYTES).description("로고 바이트")
                         )
                         )
         );
      }

      private ResultActions buildPageableResponse(ResultActions actions, int index) throws Exception {
          Club club = clubTestDataRepository.getClubs().get(index);
          S3DownloadDto s3Dto = clubTestDataRepository.getLogoS3DownloadDto(index);
          return actions
                  .andExpect(jsonPath("$.content[" + index + "].id").value(club.getId()))
                  .andExpect(jsonPath("$.content[" + index + "].name").value(club.getName()))
                  .andExpect(jsonPath("$.content[" + index + "].belongs").value(club.getBelongs()))
                  .andExpect(jsonPath("$.content[" + index + "].briefActivityDescription").value(club.getBriefActivityDescription()))
                  .andExpect(jsonPath("$.content[" + index + "].logo.id").value(s3Dto.getId()))
                  .andExpect(jsonPath("$.content[" + index + "].logo.fileName").value(s3Dto.getFileName()))
                  .andExpect(jsonPath("$.content[" + index + "].logo.bytes").value(s3Dto.getBytes()));
      }

     @Test
     public void getClubPrev_belongsEq전체_Success() throws Exception{
         //given

         //when
         ResultActions actions = mockMvc.perform(
                 get("/club/prev/{campus}/{clubType}/{belongs}", "명륜", "중앙동아리", "전체")
                         .queryParam("size", "3")
                         .queryParam("page", "2")
                         .queryParam("sortBy", "name-asc")
         );

         //then

      }

      @Test
      public void getClubPrev_ClubTypeEq전체_Success() throws Exception{
          //given

          //when
          ResultActions actions = mockMvc.perform(
                  get("/club/prev/{campus}/{clubType}/{belongs}", "명륜", "전체", "ㅁㄴㅇ")
                          .queryParam("size", "3")
                          .queryParam("page", "2")
                          .queryParam("sortBy", "name-asc")
          );

          //then

       }
    
}
