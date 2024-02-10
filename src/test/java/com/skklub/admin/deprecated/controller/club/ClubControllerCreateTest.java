package com.skklub.admin.deprecated.controller.club;

import com.skklub.admin.TestDataRepository;
import com.skklub.admin.controller.AuthValidator;
import com.skklub.admin.controller.ClubController;
import com.skklub.admin.deprecated.controller.RestDocsUtils;
import com.skklub.admin.controller.S3Transferer;
import com.skklub.admin.domain.ActivityImage;
import com.skklub.admin.domain.Club;
import com.skklub.admin.domain.Logo;
import com.skklub.admin.domain.enums.Campus;
import com.skklub.admin.domain.enums.ClubType;
import com.skklub.admin.exception.deprecated.error.exception.ClubIdMisMatchException;
import com.skklub.admin.exception.deprecated.error.exception.InvalidBelongsException;
import com.skklub.admin.repository.ClubRepository;
import com.skklub.admin.repository.UserRepository;
import com.skklub.admin.service.ClubService;
import com.skklub.admin.service.dto.FileNames;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static akka.protobuf.WireFormat.FieldType;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
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
@Import(TestDataRepository.class)
@WebMvcTest(controllers = ClubController.class)
@MockBean(JpaMetamodelMappingContext.class)
@WithMockUser
class ClubControllerCreateTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ClubService clubService;
    @MockBean
    private ClubRepository clubRepository;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private S3Transferer s3Transferer;
    @InjectMocks
    private TestDataRepository testDataRepository;
    @MockBean
    private AuthValidator authValidator;

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
        doNothing().when(authValidator).validateUpdatingClub(anyLong());
        doNothing().when(authValidator).validateUpdatingNotice(anyLong());
        doNothing().when(authValidator).validateUpdatingUser(anyLong());
        doNothing().when(authValidator).validatePendingRequestAuthority(anyLong());
    }

    @Test
    public void uploadActivityImages_MultiImages_Success() throws Exception {
        //given
        List<MultipartFile> multipartFiles = mockActivityImages.stream()
                .collect(Collectors.toList());
        List<FileNames> activityImageDtos = new ArrayList<>();
        List<ActivityImage> activityImages = new ArrayList<>();
        given(s3Transferer.uploadAll(multipartFiles)).willReturn(activityImageDtos);
        given(clubService.appendActivityImages(0L, activityImages)).willReturn(Optional.of("ClubName"));

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
                                parameterWithName("clubId").description("동아리 ID").attributes(RestDocsUtils.example("1"))
                        ),
                        requestParts(
                                partWithName("activityImages").description("활동 사진")
                        ),
                        responseFields(
                                fieldWithPath("id").type(FieldType.STRING).description("동아리 ID").attributes(RestDocsUtils.example("0")),
                                fieldWithPath("name").type(FieldType.STRING).description("동아리 이름").attributes(RestDocsUtils.example("클럽 SKKULOL"))
                        )
                ));
    }

    @Test
    public void uploadActivityImages_NoList_Fail() throws Exception {
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

    @Test
    public void uploadActivityImages_IllegalClubId_UnMatchClubException() throws Exception{
        //given
        Long clubId = -1L;
        List<FileNames> emptyList = new ArrayList<>();
        given(s3Transferer.uploadAll(anyList())).willReturn(emptyList);
        List<ActivityImage> activityImages = emptyList.stream()
                .map(FileNames::toActivityImageEntity)
                .collect(Collectors.toList());
        given(clubService.appendActivityImages(clubId, activityImages)).willReturn(Optional.empty());

        //when
        MvcResult badIdResult = mockMvc.perform(
                multipart("/club/{clubId}/activityImage", clubId)
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
        ).andExpect(status().isBadRequest()).andReturn();

        //then
        Assertions.assertThat(badIdResult.getResolvedException()).isExactlyInstanceOf(ClubIdMisMatchException.class);
     }

}