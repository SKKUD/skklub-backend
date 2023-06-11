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
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.List;

import static com.skklub.admin.controller.RestDocsUtils.addPageableResponseFields;
import static com.skklub.admin.controller.RestDocsUtils.example;
import static java.time.LocalTime.now;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import(ClubTestDataRepository.class)
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
    public void getClubPrev_FullCategory_Success() throws Exception{
        //given
        Campus campus = Campus.명륜;
        ClubType clubType = ClubType.중앙동아리;
        String belongs = "취미교양";
        int clubCnt = clubTestDataRepository.getClubCnt();
        PageRequest request = PageRequest.of(0, 5, Sort.Direction.ASC, "name");
        List<ClubPrevDTO> clubPrevs = clubTestDataRepository.getClubPrevDTOs();
        Page<ClubPrevDTO> clubPrevDTOPage = new PageImpl<>(clubPrevs, request, clubPrevs.size());

        given(clubService.getClubPrevsByCategories(campus, clubType, belongs, request)).willReturn(clubPrevDTOPage);
        clubPrevs.stream()
                .forEach(prevs -> given(s3Transferer.downloadOne(prevs.getLogo())).willReturn(clubTestDataRepository.getLogoS3DownloadDto((int) (long) prevs.getId())));
        //when
        ResultActions actions = mockMvc.perform(
                get("/club/prev")
                        .queryParam("campus", campus.toString())
                        .queryParam("clubType", clubType.toString())
                        .queryParam("belongs", belongs)
                        .queryParam("size", "5")
                        .queryParam("page", "0")
                        .queryParam("sort", "name,ASC")
        );

        //then
        actions = actions.andExpect(status().isOk());
        for(int i = 0; i < clubCnt; i++) {
            actions = buildPageableResponseContentChecker(actions, i)
                    .andExpect(jsonPath("$.size").value(5))
                    .andExpect(jsonPath("$.totalPages").value(2))
                    .andExpect(jsonPath("$.pageable.sort.sorted").value("true"));
        }
        List<FieldDescriptor> pageableResponseFields = new ArrayList<>();
        pageableResponseFields.add(fieldWithPath("content[].id").type(WireFormat.FieldType.INT64).description("동아리 아이디"));
        pageableResponseFields.add(fieldWithPath("content[].name").type(WireFormat.FieldType.STRING).description("동아리 이름"));
        pageableResponseFields.add(fieldWithPath("content[].belongs").type(WireFormat.FieldType.STRING).description("분류 - 활동 설명"));
        pageableResponseFields.add(fieldWithPath("content[].briefActivityDescription").type(WireFormat.FieldType.STRING).description("클럽 "));
        pageableResponseFields.add(fieldWithPath("content[].logo.id").type(WireFormat.FieldType.INT64).description("로고 아이디"));
        pageableResponseFields.add(fieldWithPath("content[].logo.fileName").type(WireFormat.FieldType.STRING).description("로고 원본 파일명"));
        pageableResponseFields.add(fieldWithPath("content[].logo.bytes").type(WireFormat.FieldType.BYTES).description("로고 바이트"));
        addPageableResponseFields(pageableResponseFields);

        actions.andDo(
                document("/club/get/prevs/category",
                        queryParameters(
                                parameterWithName("campus").description("분류 - 캠퍼스").attributes(example("link:common/campus-type.html[캠퍼스 종류,role=\"popup\"]")),
                                parameterWithName("clubType").description("분류 - 동아리 종류").attributes(example("link:common/club-type-null.html[동아리 종류,role=\"popup\"]")),
                                parameterWithName("belongs").description("분류 - 동아리 분과").attributes(example("link:common/belongs-null.html[분과 종류,role=\"popup\"]")),
                                parameterWithName("size").optional().description("페이지 정보 - 한 페이지 크기").attributes(example("Default : 20")),
                                parameterWithName("page").optional().description("페이지 정보 - 요청 페이지 번호(시작 0)").attributes(example("Default : 0")),
                                parameterWithName("sort").optional().description("페이지 정보 - 정렬").attributes(example("link:common/sorting.html[정렬,role=\"popup\"]"))
                        ),
                        responseFields(
                                pageableResponseFields
                        )
                )
        );
    }

    private ResultActions buildPageableResponseContentChecker(ResultActions actions, int index) throws Exception {
        Club club = clubTestDataRepository.getClubs().get(index);
        S3DownloadDto s3Dto = clubTestDataRepository.getLogoS3DownloadDto(index);
        return actions
                .andExpect(jsonPath("$.content[" + index + "].id").value(index))
                .andExpect(jsonPath("$.content[" + index + "].name").value(club.getName()))
                .andExpect(jsonPath("$.content[" + index + "].belongs").value(club.getBelongs()))
                .andExpect(jsonPath("$.content[" + index + "].briefActivityDescription").value(club.getBriefActivityDescription()))
                .andExpect(jsonPath("$.content[" + index + "].logo.id").value(s3Dto.getId()))
                .andExpect(jsonPath("$.content[" + index + "].logo.fileName").value(s3Dto.getFileName()))
                .andExpect(jsonPath("$.content[" + index + "].logo.bytes").value(s3Dto.getBytes()));
    }


    @Test
    public void getClubPrev_NoBelongs_Success() throws Exception{
        //given
        Campus campus = Campus.명륜;
        ClubType clubType = ClubType.중앙동아리;
        String belongs = "전체";
        int clubCnt = clubTestDataRepository.getClubCnt();
        PageRequest request = PageRequest.of(0, 5, Sort.Direction.ASC, "name");
        List<ClubPrevDTO> clubPrevs = clubTestDataRepository.getClubPrevDTOs();
        Page<ClubPrevDTO> clubPrevDTOPage = new PageImpl<>(clubPrevs, request, clubPrevs.size());

        given(clubService.getClubPrevsByCategories(campus, clubType, belongs, request)).willReturn(clubPrevDTOPage);
        clubPrevs.stream()
                .forEach(prevs -> given(s3Transferer.downloadOne(prevs.getLogo())).willReturn(clubTestDataRepository.getLogoS3DownloadDto((int) (long) prevs.getId())));
        //when
        ResultActions actions = mockMvc.perform(
                get("/club/prev")
                        .queryParam("campus", campus.toString())
                        .queryParam("clubType", clubType.toString())
                        .queryParam("size", "5")
                        .queryParam("page", "0")
                        .queryParam("sort", "name,ASC")
        );

        //then
        actions = actions.andExpect(status().isOk());
        for(int i = 0; i < clubCnt; i++) {
            actions = buildPageableResponseContentChecker(actions, i)
                    .andExpect(jsonPath("$.size").value(5))
                    .andExpect(jsonPath("$.totalPages").value(2))
                    .andExpect(jsonPath("$.pageable.sort.sorted").value("true"));
        }
    }

    @Test
    public void getClubPrev_NoClubTypeAndAnyBelongs_Success() throws Exception{
        //given
        Campus campus = Campus.명륜;
        ClubType clubType = ClubType.전체;
        String belongs = "AnyBelongsString";
        int clubCnt = clubTestDataRepository.getClubCnt();
        PageRequest request = PageRequest.of(0, 5, Sort.Direction.ASC, "name");
        List<ClubPrevDTO> clubPrevs = clubTestDataRepository.getClubPrevDTOs();
        Page<ClubPrevDTO> clubPrevDTOPage = new PageImpl<>(clubPrevs, request, clubPrevs.size());

        given(clubService.getClubPrevsByCategories(campus, clubType, belongs, request)).willReturn(clubPrevDTOPage);
        clubPrevs.stream()
                .forEach(prevs -> given(s3Transferer.downloadOne(prevs.getLogo())).willReturn(clubTestDataRepository.getLogoS3DownloadDto((int) (long) prevs.getId())));
        //when
        ResultActions actions = mockMvc.perform(
                get("/club/prev")
                        .queryParam("campus", campus.toString())
                        .queryParam("belongs", belongs)
                        .queryParam("size", "5")
                        .queryParam("page", "0")
                        .queryParam("sort", "name,ASC")
        );

        //then
        actions = actions.andExpect(status().isOk());
        for(int i = 0; i < clubCnt; i++) {
            actions = buildPageableResponseContentChecker(actions, i)
                    .andExpect(jsonPath("$.size").value(5))
                    .andExpect(jsonPath("$.totalPages").value(2))
                    .andExpect(jsonPath("$.pageable.sort.sorted").value("true"));
        }
    }

    @Test
    public void getClubPrev_NoClubTypeAndNoBelongs_Success() throws Exception{
        //given
        Campus campus = Campus.명륜;
        ClubType clubType = ClubType.전체;
        String belongs = "전체";
        int clubCnt = clubTestDataRepository.getClubCnt();
        PageRequest request = PageRequest.of(0, 5, Sort.Direction.ASC, "name");
        List<ClubPrevDTO> clubPrevs = clubTestDataRepository.getClubPrevDTOs();
        Page<ClubPrevDTO> clubPrevDTOPage = new PageImpl<>(clubPrevs, request, clubPrevs.size());

        given(clubService.getClubPrevsByCategories(campus, clubType, belongs, request)).willReturn(clubPrevDTOPage);
        clubPrevs.stream()
                .forEach(prevs -> given(s3Transferer.downloadOne(prevs.getLogo())).willReturn(clubTestDataRepository.getLogoS3DownloadDto((int) (long) prevs.getId())));
        //when
        ResultActions actions = mockMvc.perform(
                get("/club/prev")
                        .queryParam("campus", campus.toString())
                        .queryParam("size", "5")
                        .queryParam("page", "0")
                        .queryParam("sort", "name,ASC")
        );

        //then
        actions = actions.andExpect(status().isOk());
        for(int i = 0; i < clubCnt; i++) {
            actions = buildPageableResponseContentChecker(actions, i)
                    .andExpect(jsonPath("$.size").value(5))
                    .andExpect(jsonPath("$.totalPages").value(2))
                    .andExpect(jsonPath("$.pageable.sort.sorted").value("true"));
        }
    }

}


