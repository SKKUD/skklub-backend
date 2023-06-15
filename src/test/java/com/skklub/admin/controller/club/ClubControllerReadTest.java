package com.skklub.admin.controller.club;

import akka.protobuf.WireFormat;
import com.skklub.admin.controller.ClubController;
import com.skklub.admin.controller.ClubTestDataRepository;
import com.skklub.admin.controller.RestDocsUtils;
import com.skklub.admin.controller.S3Transferer;
import com.skklub.admin.controller.dto.RecruitDto;
import com.skklub.admin.controller.dto.S3DownloadDto;
import com.skklub.admin.domain.Club;
import com.skklub.admin.domain.enums.Campus;
import com.skklub.admin.domain.enums.ClubType;
import com.skklub.admin.service.dto.ClubDetailInfoDto;
import com.skklub.admin.service.dto.ClubPrevDTO;
import com.skklub.admin.service.ClubService;
import com.skklub.admin.service.dto.FileNames;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.skklub.admin.controller.RestDocsUtils.*;
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
@WithMockUser
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
    public void getClubById_Default_Success() throws Exception{
        //given
        long clubId = 0L;
        ClubDetailInfoDto clubDetailInfoDto = clubTestDataRepository.getClubDetailInfoDtos().get((int) clubId);
        S3DownloadDto logoS3DownloadDto = clubTestDataRepository.getLogoS3DownloadDto((int) clubId);
        List<S3DownloadDto> activityImgS3DownloadDtos = clubTestDataRepository.getActivityImgS3DownloadDtos((int) clubId);
        given(clubService.getClubDetailInfoById(clubId)).willReturn(Optional.of(clubDetailInfoDto));
        given(s3Transferer.downloadOne(clubDetailInfoDto.getLogo())).willReturn(logoS3DownloadDto);
        given(s3Transferer.downloadAll(clubDetailInfoDto.getActivityImages())).willReturn(activityImgS3DownloadDtos);

        //when
        ResultActions actions = mockMvc.perform(
                get("/club/{clubId}", clubId)
        );

        //then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clubDetailInfoDto.getId()))
                .andExpect(jsonPath("$.campus").value(clubDetailInfoDto.getCampus().toString()))
                .andExpect(jsonPath("$.clubType").value(clubDetailInfoDto.getClubType()))
                .andExpect(jsonPath("$.belongs").value(clubDetailInfoDto.getBelongs()))
                .andExpect(jsonPath("$.briefActivityDescription").value(clubDetailInfoDto.getBriefActivityDescription()))
                .andExpect(jsonPath("$.name").value(clubDetailInfoDto.getName()))
                .andExpect(jsonPath("$.headLine").value(clubDetailInfoDto.getHeadLine()))
                .andExpect(jsonPath("$.establishAt").value(clubDetailInfoDto.getEstablishAt()))
                .andExpect(jsonPath("$.roomLocation").value(clubDetailInfoDto.getRoomLocation()))
                .andExpect(jsonPath("$.memberAmount").value(clubDetailInfoDto.getMemberAmount()))
                .andExpect(jsonPath("$.regularMeetingTime").value(clubDetailInfoDto.getRegularMeetingTime()))
                .andExpect(jsonPath("$.mandatoryActivatePeriod").value(clubDetailInfoDto.getMandatoryActivatePeriod()))
                .andExpect(jsonPath("$.clubDescription").value(clubDetailInfoDto.getClubDescription()))
                .andExpect(jsonPath("$.activityDescription").value(clubDetailInfoDto.getActivityDescription()))
                .andExpect(jsonPath("$.webLink1").value(clubDetailInfoDto.getWebLink1()))
                .andExpect(jsonPath("$.webLink2").value(clubDetailInfoDto.getWebLink2()))
                .andExpect(jsonPath("$.presidentName").value(clubDetailInfoDto.getPresidentName()))
                .andExpect(jsonPath("$.presidentContact").value(clubDetailInfoDto.getPresidentContact()))
                .andExpect(jsonPath("$.logo.id").value(logoS3DownloadDto.getId()))
                .andExpect(jsonPath("$.logo.fileName").value(logoS3DownloadDto.getFileName()))
                .andExpect(jsonPath("$.logo.bytes").value(logoS3DownloadDto.getBytes()));
                clubDetailInfoDto.getRecruit().ifPresent(r -> {
                    try {
                        checkRecruitResponseJson(actions, r);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                for(int i = 0; i < clubTestDataRepository.getActivityImgPerClub(); i++) {
                    checkActivityImagesResponseJson(actions, i, activityImgS3DownloadDtos);
                }
        actions.andDo(
                document("/club/get/detail/id",
                        pathParameters(
                                parameterWithName("clubId").attributes(example("1")).description("동아리 ID")
                        ),
                        responseFields(
                                fieldWithPath("id").type(WireFormat.FieldType.INT64).description("동아리 Id").attributes(example(clubDetailInfoDto.getId().toString())),
                                fieldWithPath("campus").type(WireFormat.FieldType.STRING).description("분류 - 캠퍼스").attributes(example(clubDetailInfoDto.getCampus().toString())),
                                fieldWithPath("clubType").type(WireFormat.FieldType.STRING).description("분류 - 동아리 종류").attributes(example(clubDetailInfoDto.getClubType())),
                                fieldWithPath("belongs").type(WireFormat.FieldType.STRING).description("분류 - 동아리 분과").attributes(example(clubDetailInfoDto.getBelongs())),
                                fieldWithPath("briefActivityDescription").type(WireFormat.FieldType.STRING).description("분류 - 활동 설명").attributes(example(clubDetailInfoDto.getBriefActivityDescription())),
                                fieldWithPath("name").type(WireFormat.FieldType.STRING).description("동아리 이름").attributes(example(clubDetailInfoDto.getName())),
                                fieldWithPath("headLine").type(WireFormat.FieldType.STRING).description("한줄 소개").attributes(example(clubDetailInfoDto.getHeadLine())),
                                fieldWithPath("establishAt").type(WireFormat.FieldType.STRING).description("설립 연도").attributes(example(clubDetailInfoDto.getEstablishAt())),
                                fieldWithPath("roomLocation").type(WireFormat.FieldType.STRING).description("동아리 방 위치").attributes(example(clubDetailInfoDto.getRoomLocation())),
                                fieldWithPath("memberAmount").type(WireFormat.FieldType.STRING).description("동아리 인원").attributes(example(clubDetailInfoDto.getMemberAmount().toString())),
                                fieldWithPath("regularMeetingTime").type(WireFormat.FieldType.STRING).description("정규 모임 시간").attributes(example(clubDetailInfoDto.getRegularMeetingTime())),
                                fieldWithPath("mandatoryActivatePeriod").type(WireFormat.FieldType.STRING).description("의무 활동 기간").attributes(example(clubDetailInfoDto.getMandatoryActivatePeriod())),
                                fieldWithPath("clubDescription").type(WireFormat.FieldType.STRING).description("자세한 동아리 설명").attributes(example(clubDetailInfoDto.getClubDescription())),
                                fieldWithPath("activityDescription").type(WireFormat.FieldType.STRING).description("자세한 활동 설명").attributes(example(clubDetailInfoDto.getActivityDescription())),
                                fieldWithPath("webLink1").type(WireFormat.FieldType.STRING).description("관련 사이트 주소 1").attributes(example(clubDetailInfoDto.getWebLink1())),
                                fieldWithPath("webLink2").type(WireFormat.FieldType.STRING).description("관련 사이트 주소 2").attributes(example(clubDetailInfoDto.getWebLink2())),
                                fieldWithPath("recruit.recruitStartAt").type(WireFormat.FieldType.STRING).description("모집 시작일").attributes(example("yyyy-MM-ddTHH:mm (null when not recruting)")),
                                fieldWithPath("recruit.recruitEndAt").type(WireFormat.FieldType.STRING).description("모집 종료일").attributes(example("yyyy-MM-ddTHH:mm (null when not recruting)")),
                                fieldWithPath("recruit.recruitQuota").type(WireFormat.FieldType.STRING).description("모집 인원").attributes(example("10 ~ 30명 - Can Any Format(null when not recruting)")),
                                fieldWithPath("recruit.recruitProcessDescription").type(WireFormat.FieldType.STRING).description("모집 절차 설명").attributes(example("Test Recruit Format(null when not recruting)")),
                                fieldWithPath("recruit.recruitContact").type(WireFormat.FieldType.STRING).description("모집 문의처").attributes(example("010-1234-1234 or recruit@asd.asd - Can Any Format(null when not recruting)")),
                                fieldWithPath("recruit.recruitWebLink").type(WireFormat.FieldType.STRING).description("모집 링크").attributes(example("form.goole.com - Can Any Format(null when not recruting)")),
                                fieldWithPath("presidentName").type(WireFormat.FieldType.STRING).description("회장 이름").attributes(example(clubDetailInfoDto.getPresidentName())),
                                fieldWithPath("presidentContact").type(WireFormat.FieldType.STRING).description("회장 연락처").attributes(example(clubDetailInfoDto.getPresidentContact())),
                                fieldWithPath("logo.id").type(WireFormat.FieldType.INT64).description("로고 ID").attributes(example(logoS3DownloadDto.getId().toString())),
                                fieldWithPath("logo.fileName").type(WireFormat.FieldType.STRING).description("로고 파일명").attributes(example(logoS3DownloadDto.getFileName())),
                                fieldWithPath("logo.bytes").type(WireFormat.FieldType.STRING).description("로고 바이트(파일)").attributes(example(logoS3DownloadDto.getBytes())),
                                fieldWithPath("activityImages[].id").type(WireFormat.FieldType.STRING).description("활동 사진 ID").attributes(example(activityImgS3DownloadDtos.get(0).getId().toString())),
                                fieldWithPath("activityImages[].fileName").type(WireFormat.FieldType.STRING).description("활동 사진 파일명").attributes(example(activityImgS3DownloadDtos.get(0).getFileName())),
                                fieldWithPath("activityImages.[]bytes").type(WireFormat.FieldType.STRING).description("활동 사진 바이트(파일)") .attributes(example(activityImgS3DownloadDtos.get(0).getBytes()))
                        )
                )
        );
     }

    @Test
    public void getClubByName_Default_Success() throws Exception{
        //given
        long clubId = 0L;
        ClubDetailInfoDto clubDetailInfoDto = clubTestDataRepository.getClubDetailInfoDtos().get((int) clubId);
        String clubName = clubDetailInfoDto.getName();
        S3DownloadDto logoS3DownloadDto = clubTestDataRepository.getLogoS3DownloadDto((int) clubId);
        List<S3DownloadDto> activityImgS3DownloadDtos = clubTestDataRepository.getActivityImgS3DownloadDtos((int) clubId);
        given(clubService.getClubDetailInfoByName(clubName)).willReturn(Optional.of(clubDetailInfoDto));
        given(s3Transferer.downloadOne(clubDetailInfoDto.getLogo())).willReturn(logoS3DownloadDto);
        given(s3Transferer.downloadAll(clubDetailInfoDto.getActivityImages())).willReturn(activityImgS3DownloadDtos);

        //when
        ResultActions actions = mockMvc.perform(
                get("/club/search")
                        .queryParam("name", clubName)
        );

        //then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clubDetailInfoDto.getId()))
                .andExpect(jsonPath("$.campus").value(clubDetailInfoDto.getCampus().toString()))
                .andExpect(jsonPath("$.clubType").value(clubDetailInfoDto.getClubType()))
                .andExpect(jsonPath("$.belongs").value(clubDetailInfoDto.getBelongs()))
                .andExpect(jsonPath("$.briefActivityDescription").value(clubDetailInfoDto.getBriefActivityDescription()))
                .andExpect(jsonPath("$.name").value(clubDetailInfoDto.getName()))
                .andExpect(jsonPath("$.headLine").value(clubDetailInfoDto.getHeadLine()))
                .andExpect(jsonPath("$.establishAt").value(clubDetailInfoDto.getEstablishAt()))
                .andExpect(jsonPath("$.roomLocation").value(clubDetailInfoDto.getRoomLocation()))
                .andExpect(jsonPath("$.memberAmount").value(clubDetailInfoDto.getMemberAmount()))
                .andExpect(jsonPath("$.regularMeetingTime").value(clubDetailInfoDto.getRegularMeetingTime()))
                .andExpect(jsonPath("$.mandatoryActivatePeriod").value(clubDetailInfoDto.getMandatoryActivatePeriod()))
                .andExpect(jsonPath("$.clubDescription").value(clubDetailInfoDto.getClubDescription()))
                .andExpect(jsonPath("$.activityDescription").value(clubDetailInfoDto.getActivityDescription()))
                .andExpect(jsonPath("$.webLink1").value(clubDetailInfoDto.getWebLink1()))
                .andExpect(jsonPath("$.webLink2").value(clubDetailInfoDto.getWebLink2()))
                .andExpect(jsonPath("$.presidentName").value(clubDetailInfoDto.getPresidentName()))
                .andExpect(jsonPath("$.presidentContact").value(clubDetailInfoDto.getPresidentContact()))
                .andExpect(jsonPath("$.logo.id").value(logoS3DownloadDto.getId()))
                .andExpect(jsonPath("$.logo.fileName").value(logoS3DownloadDto.getFileName()))
                .andExpect(jsonPath("$.logo.bytes").value(logoS3DownloadDto.getBytes()));
        clubDetailInfoDto.getRecruit().ifPresent(r -> {
            try {
                checkRecruitResponseJson(actions, r);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        for(int i = 0; i < clubTestDataRepository.getActivityImgPerClub(); i++) {
            checkActivityImagesResponseJson(actions, i, activityImgS3DownloadDtos);
        }
        actions.andDo(
                document("/club/get/detail/search",
                        queryParameters(
                                parameterWithName("name").attributes(example(clubName)).description("동아리 이름(정확히 일치 시에만)")
                        ),
                        responseFields(
                                fieldWithPath("id").type(WireFormat.FieldType.INT64).description("동아리 Id").attributes(example(clubDetailInfoDto.getId().toString())),
                                fieldWithPath("campus").type(WireFormat.FieldType.STRING).description("분류 - 캠퍼스").attributes(example(clubDetailInfoDto.getCampus().toString())),
                                fieldWithPath("clubType").type(WireFormat.FieldType.STRING).description("분류 - 동아리 종류").attributes(example(clubDetailInfoDto.getClubType())),
                                fieldWithPath("belongs").type(WireFormat.FieldType.STRING).description("분류 - 동아리 분과").attributes(example(clubDetailInfoDto.getBelongs())),
                                fieldWithPath("briefActivityDescription").type(WireFormat.FieldType.STRING).description("분류 - 활동 설명").attributes(example(clubDetailInfoDto.getBriefActivityDescription())),
                                fieldWithPath("name").type(WireFormat.FieldType.STRING).description("동아리 이름").attributes(example(clubDetailInfoDto.getName())),
                                fieldWithPath("headLine").type(WireFormat.FieldType.STRING).description("한줄 소개").attributes(example(clubDetailInfoDto.getHeadLine())),
                                fieldWithPath("establishAt").type(WireFormat.FieldType.STRING).description("설립 연도").attributes(example(clubDetailInfoDto.getEstablishAt())),
                                fieldWithPath("roomLocation").type(WireFormat.FieldType.STRING).description("동아리 방 위치").attributes(example(clubDetailInfoDto.getRoomLocation())),
                                fieldWithPath("memberAmount").type(WireFormat.FieldType.STRING).description("동아리 인원").attributes(example(clubDetailInfoDto.getMemberAmount().toString())),
                                fieldWithPath("regularMeetingTime").type(WireFormat.FieldType.STRING).description("정규 모임 시간").attributes(example(clubDetailInfoDto.getRegularMeetingTime())),
                                fieldWithPath("mandatoryActivatePeriod").type(WireFormat.FieldType.STRING).description("의무 활동 기간").attributes(example(clubDetailInfoDto.getMandatoryActivatePeriod())),
                                fieldWithPath("clubDescription").type(WireFormat.FieldType.STRING).description("자세한 동아리 설명").attributes(example(clubDetailInfoDto.getClubDescription())),
                                fieldWithPath("activityDescription").type(WireFormat.FieldType.STRING).description("자세한 활동 설명").attributes(example(clubDetailInfoDto.getActivityDescription())),
                                fieldWithPath("webLink1").type(WireFormat.FieldType.STRING).description("관련 사이트 주소 1").attributes(example(clubDetailInfoDto.getWebLink1())),
                                fieldWithPath("webLink2").type(WireFormat.FieldType.STRING).description("관련 사이트 주소 2").attributes(example(clubDetailInfoDto.getWebLink2())),
                                fieldWithPath("recruit.recruitStartAt").type(WireFormat.FieldType.STRING).description("모집 시작일").attributes(example("yyyy-MM-ddTHH:mm (null when not recruting)")),
                                fieldWithPath("recruit.recruitEndAt").type(WireFormat.FieldType.STRING).description("모집 종료일").attributes(example("yyyy-MM-ddTHH:mm (null when not recruting)")),
                                fieldWithPath("recruit.recruitQuota").type(WireFormat.FieldType.STRING).description("모집 인원").attributes(example("10 ~ 30명 - Can Any Format(null when not recruting)")),
                                fieldWithPath("recruit.recruitProcessDescription").type(WireFormat.FieldType.STRING).description("모집 절차 설명").attributes(example("Test Recruit Format(null when not recruting)")),
                                fieldWithPath("recruit.recruitContact").type(WireFormat.FieldType.STRING).description("모집 문의처").attributes(example("010-1234-1234 or recruit@asd.asd - Can Any Format(null when not recruting)")),
                                fieldWithPath("recruit.recruitWebLink").type(WireFormat.FieldType.STRING).description("모집 링크").attributes(example("form.goole.com - Can Any Format(null when not recruting)")),
                                fieldWithPath("presidentName").type(WireFormat.FieldType.STRING).description("회장 이름").attributes(example(clubDetailInfoDto.getPresidentName())),
                                fieldWithPath("presidentContact").type(WireFormat.FieldType.STRING).description("회장 연락처").attributes(example(clubDetailInfoDto.getPresidentContact())),
                                fieldWithPath("logo.id").type(WireFormat.FieldType.INT64).description("로고 ID").attributes(example(logoS3DownloadDto.getId().toString())),
                                fieldWithPath("logo.fileName").type(WireFormat.FieldType.STRING).description("로고 파일명").attributes(example(logoS3DownloadDto.getFileName())),
                                fieldWithPath("logo.bytes").type(WireFormat.FieldType.STRING).description("로고 바이트(파일)").attributes(example(logoS3DownloadDto.getBytes())),
                                fieldWithPath("activityImages[].id").type(WireFormat.FieldType.STRING).description("활동 사진 ID").attributes(example(activityImgS3DownloadDtos.get(0).getId().toString())),
                                fieldWithPath("activityImages[].fileName").type(WireFormat.FieldType.STRING).description("활동 사진 파일명").attributes(example(activityImgS3DownloadDtos.get(0).getFileName())),
                                fieldWithPath("activityImages.[]bytes").type(WireFormat.FieldType.STRING).description("활동 사진 바이트(파일)") .attributes(example(activityImgS3DownloadDtos.get(0).getBytes()))
                        )
                )
        );
    }

    private void checkActivityImagesResponseJson(ResultActions actions, int activityImgIndex, List<S3DownloadDto> activityImgS3DownloadDtos) throws Exception {
        actions.andExpect(jsonPath("$.activityImages[" + activityImgIndex + "].id").value(activityImgS3DownloadDtos.get(activityImgIndex).getId()))
                .andExpect(jsonPath("$.activityImages[" + activityImgIndex + "].fileName").value(activityImgS3DownloadDtos.get(activityImgIndex).getFileName()))
                .andExpect(jsonPath("$.activityImages[" + activityImgIndex + "].bytes").value(activityImgS3DownloadDtos.get(activityImgIndex).getBytes()));

    }
    private void checkRecruitResponseJson(ResultActions actions, RecruitDto recruitDto) throws Exception {
        actions.andExpect(jsonPath("$.recruit.recruitStartAt").value(recruitDto.getRecruitStartAt().toString()))
                .andExpect(jsonPath("$.recruit.recruitEndAt").value(recruitDto.getRecruitEndAt().toString()))
                .andExpect(jsonPath("$.recruit.recruitQuota").value(recruitDto.getRecruitQuota()))
                .andExpect(jsonPath("$.recruit.recruitProcessDescription").value(recruitDto.getRecruitProcessDescription()))
                .andExpect(jsonPath("$.recruit.recruitContact").value(recruitDto.getRecruitContact()))
                .andExpect(jsonPath("$.recruit.recruitWebLink").value(recruitDto.getRecruitWebLink()));
    }

    @Test
    public void getClubPrev_FullCategory_Success() throws Exception{
        //given
        Campus campus = Campus.명륜;
        ClubType clubType = ClubType.중앙동아리;
        String belongs = "취미교양";
        int clubPerPage = 5;
        PageRequest request = PageRequest.of(0, clubPerPage, Sort.Direction.ASC, "name");
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
                        .queryParam("size", String.valueOf(clubPerPage))
                        .queryParam("page", "0")
                        .queryParam("sort", "name,ASC")
        );

        //then
        actions = actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(clubPerPage))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.pageable.sort.sorted").value("true"));
        for(int i = 0; i < clubPerPage; i++) {
            actions = buildPageableResponseContentChecker(actions, i);
        }

        List<FieldDescriptor> pageableResponseFields = new ArrayList<>();
        pageableResponseFields.add(fieldWithPath("content[].id").type(WireFormat.FieldType.INT64).description("동아리 아이디").attributes(example(clubPrevs.get(0).getId().toString())));
        pageableResponseFields.add(fieldWithPath("content[].name").type(WireFormat.FieldType.STRING).description("동아리 이름").attributes(example(clubPrevs.get(0).getName())));
        pageableResponseFields.add(fieldWithPath("content[].belongs").type(WireFormat.FieldType.STRING).description("분류 - 활동 설명").attributes(example(clubPrevs.get(0).getBelongs())));
        pageableResponseFields.add(fieldWithPath("content[].briefActivityDescription").type(WireFormat.FieldType.STRING).description("클럽 ").attributes(example(clubPrevs.get(0).getBriefActivityDescription())));
        pageableResponseFields.add(fieldWithPath("content[].logo.id").type(WireFormat.FieldType.INT64).description("로고 아이디").attributes(example(clubTestDataRepository.getLogoS3DownloadDto((int) (long) clubPrevs.get(0).getId()).getId().toString())));
        pageableResponseFields.add(fieldWithPath("content[].logo.fileName").type(WireFormat.FieldType.STRING).description("로고 원본 파일명").attributes(example(clubTestDataRepository.getLogoS3DownloadDto((int) (long) clubPrevs.get(0).getId()).getFileName())));
        pageableResponseFields.add(fieldWithPath("content[].logo.bytes").type(WireFormat.FieldType.BYTES).description("로고 바이트").attributes(example(clubTestDataRepository.getLogoS3DownloadDto((int) (long) clubPrevs.get(0).getId()).getBytes())));
        addPageableResponseFields(pageableResponseFields);

        actions.andDo(
                document("/club/get/prevs/category",
                        queryParameters(
                                parameterWithName("campus").description("분류 - 캠퍼스").attributes(example(LINK_CAMPUS_TYPE)),
                                parameterWithName("clubType").description("분류 - 동아리 종류").attributes(example(LINK_CLUB_TYPE_NULL)),
                                parameterWithName("belongs").description("분류 - 동아리 분과").attributes(example(LINK_BELONGS_TYPE_NULL)),
                                parameterWithName("size").optional().description("페이지 정보 - 한 페이지 크기").attributes(example("Default : 20")),
                                parameterWithName("page").optional().description("페이지 정보 - 요청 페이지 번호(시작 0)").attributes(example("Default : 0")),
                                parameterWithName("sort").optional().description("페이지 정보 - 정렬").attributes(example(RestDocsUtils.LINK_SORT))
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
        int clubPerPage = 5;
        PageRequest request = PageRequest.of(0, clubPerPage, Sort.Direction.ASC, "name");
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
                        .queryParam("size", String.valueOf(clubPerPage))
                        .queryParam("page", "0")
                        .queryParam("sort", "name,ASC")
        );

        //then
        actions = actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(clubPerPage))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.pageable.sort.sorted").value("true"));
        for(int i = 0; i < clubPerPage; i++) {
            actions = buildPageableResponseContentChecker(actions, i);
        }
    }

    @Test
    public void getClubPrev_NoClubTypeAndAnyBelongs_Success() throws Exception{
        //given
        Campus campus = Campus.명륜;
        ClubType clubType = ClubType.전체;
        String belongs = "AnyBelongsString";
        int clubPerPage = 5;
        PageRequest request = PageRequest.of(0, clubPerPage, Sort.Direction.ASC, "name");
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
                        .queryParam("size", String.valueOf(clubPerPage))
                        .queryParam("page", "0")
                        .queryParam("sort", "name,ASC")
        );

        //then
        actions = actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(clubPerPage))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.pageable.sort.sorted").value("true"));
        for(int i = 0; i < clubPerPage; i++) {
            actions = buildPageableResponseContentChecker(actions, i);
        }
    }

    @Test
    public void getClubPrev_NoClubTypeAndNoBelongs_Success() throws Exception{
        //given
        Campus campus = Campus.명륜;
        ClubType clubType = ClubType.전체;
        String belongs = "전체";
        int clubPerPage = 5;
        PageRequest request = PageRequest.of(0, clubPerPage, Sort.Direction.ASC, "name");
        List<ClubPrevDTO> clubPrevs = clubTestDataRepository.getClubPrevDTOs();
        Page<ClubPrevDTO> clubPrevDTOPage = new PageImpl<>(clubPrevs, request, clubPrevs.size());

        given(clubService.getClubPrevsByCategories(campus, clubType, belongs, request)).willReturn(clubPrevDTOPage);
        clubPrevs.stream()
                .forEach(prevs -> given(s3Transferer.downloadOne(prevs.getLogo())).willReturn(clubTestDataRepository.getLogoS3DownloadDto((int) (long) prevs.getId())));
        //when
        ResultActions actions = mockMvc.perform(
                get("/club/prev")
                        .queryParam("campus", campus.toString())
                        .queryParam("size", String.valueOf(clubPerPage))
                        .queryParam("page", "0")
                        .queryParam("sort", "name,ASC")
        );

        //then
        actions = actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(clubPerPage))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.pageable.sort.sorted").value("true"));
        for(int i = 0; i < clubPerPage; i++) {
            actions = buildPageableResponseContentChecker(actions, i);
        }
    }

    @Test
    public void getClubPrevByKeyword_Default_Success() throws Exception{
        //given
        String keyword = "SKKU";
        int clubPerPage = 5;
        PageRequest request = PageRequest.of(0, clubPerPage, Sort.Direction.ASC, "name");
        List<ClubPrevDTO> clubPrevDTOs = clubTestDataRepository.getClubPrevDTOs();
        Page<ClubPrevDTO> clubPrevDTOPage = new PageImpl<>(clubPrevDTOs, request, clubPrevDTOs.size());
        given(clubService.getClubPrevsByKeyword(keyword, request)).willReturn(clubPrevDTOPage);
        clubPrevDTOs.stream()
                .forEach(prevs -> given(s3Transferer.downloadOne(prevs.getLogo())).willReturn(clubTestDataRepository.getLogoS3DownloadDto((int) (long) prevs.getId())));

        //when
        ResultActions actions = mockMvc.perform(
                get("/club/search/prevs")
                        .queryParam("keyword", keyword)
                        .queryParam("size", String.valueOf(clubPerPage))
                        .queryParam("page", "0")
                        .queryParam("sort", "name,ASC")
        );

        //then
        actions = actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(clubPerPage))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.pageable.sort.sorted").value("true"));
        for(int i = 0; i < clubPerPage; i++) {
            actions = buildPageableResponseContentChecker(actions, i);
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
                document("/club/get/prevs/search",
                        queryParameters(
                                parameterWithName("keyword").description("동아리 이름 검색 키워드").attributes(example("%Keyword%")),
                                parameterWithName("size").optional().description("페이지 정보 - 한 페이지 크기").attributes(example("Default : 20")),
                                parameterWithName("page").optional().description("페이지 정보 - 요청 페이지 번호(시작 0)").attributes(example("Default : 0")),
                                parameterWithName("sort").optional().description("페이지 정보 - 정렬").attributes(example(RestDocsUtils.LINK_SORT))
                        ),
                        responseFields(
                                pageableResponseFields
                        )
                )
        );

     }

     @Test
     public void getRandomClubNameAndIdByCategories_Default_Success() throws Exception{
         //given
         Campus campus = Campus.명륜;
         ClubType clubType = ClubType.중앙동아리;
         String belongs = "취미교양";
         List<ClubPrevDTO> clubPrevDTOs = clubTestDataRepository.getClubPrevDTOs();
         given(clubService.getRandomClubsByCategories(campus, clubType, belongs)).willReturn(clubPrevDTOs);

         //when
         ResultActions actions = mockMvc.perform(
                 get("/club/random")
                         .queryParam("campus", campus.toString())
                         .queryParam("clubType", clubType.toString())
                         .queryParam("belongs", belongs)
         );

         //then
         actions.andExpect(status().isOk());
         for(int i = 0; i < clubPrevDTOs.size(); i++) {
             actions.andExpect(jsonPath("$["+i+"].id").value(clubPrevDTOs.get(i).getId()))
                     .andExpect(jsonPath("$["+i+"].name").value(clubPrevDTOs.get(i).getName()));
         }
         actions.andDo(
                 document("/club/get/random",
                         queryParameters(
                                 parameterWithName("campus").description("분류 - 캠퍼스").attributes(example(LINK_CAMPUS_TYPE)),
                                 parameterWithName("clubType").description("분류 - 동아리 종류").attributes(example(LINK_CLUB_TYPE_NULL)),
                                 parameterWithName("belongs").description("분류 - 동아리 분과").attributes(example(LINK_BELONGS_TYPE_NULL))
                         ),
                         responseFields(
                                 fieldWithPath("[]id").type(WireFormat.FieldType.INT64).description("동아리 Id").attributes(example(clubPrevDTOs.get(0).getId().toString())),
                                 fieldWithPath("[]name").type(WireFormat.FieldType.STRING).description("동아리 이름").attributes(example(clubPrevDTOs.get(0).getName()))
                         )
                 )
         );
      }

}