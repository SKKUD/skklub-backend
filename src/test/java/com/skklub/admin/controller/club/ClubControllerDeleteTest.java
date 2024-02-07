package com.skklub.admin.controller.club;

import akka.protobuf.WireFormat;
import com.skklub.admin.controller.AuthValidator;
import com.skklub.admin.controller.ClubController;
import com.skklub.admin.controller.S3Transferer;
import com.skklub.admin.exception.deprecated.error.exception.ActivityImageMisMatchException;
import com.skklub.admin.exception.deprecated.error.exception.MissingAliveClubException;
import com.skklub.admin.exception.deprecated.error.exception.MissingDeletedClubException;
import com.skklub.admin.repository.ClubRepository;
import com.skklub.admin.repository.UserRepository;
import com.skklub.admin.service.ClubService;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Optional;

import static com.skklub.admin.controller.RestDocsUtils.example;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@WithMockUser
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@WebMvcTest(controllers = ClubController.class)
@MockBean(JpaMetamodelMappingContext.class)
class ClubControllerDeleteTest {
    @MockBean
    private ClubService clubService;
    @MockBean
    private ClubRepository clubRepository;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private S3Transferer s3Transferer;
    @MockBean
    private AuthValidator authValidator;
    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void beforeEach() {
        doNothing().when(authValidator).validateUpdatingClub(anyLong());
        doNothing().when(authValidator).validateUpdatingNotice(anyLong());
        doNothing().when(authValidator).validateUpdatingUser(anyLong());
        doNothing().when(authValidator).validatePendingRequestAuthority(anyLong());
    }

    @Test
    public void deleteClubById_Default_Success() throws Exception {
        //given
        given(clubService.deleteClub(0L)).willReturn(Optional.of("Test Club Name"));

        //when
        ResultActions actions = mockMvc.perform(
                delete("/club/{clubId}", 0L)
                        .with(csrf())
        );

        //then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(0L))
                .andExpect(jsonPath("$.name").value("Test Club Name"))
                .andDo(
                        document("club/delete/club",
                                pathParameters(
                                        parameterWithName("clubId").description("동아리 ID").attributes(example("1"))
                                ),
                                responseFields(
                                        fieldWithPath("id").type(WireFormat.FieldType.INT64).description("삭제된 동아리 ID").attributes(example("1")),
                                        fieldWithPath("name").type(WireFormat.FieldType.STRING).description("삭제된 동아리 이름").attributes(example("Test Club Name"))
                                )
                        )
                );
    }

    @Test
    public void deleteClubById_NoAliveClubExist_MissingAliveClubException() throws Exception{
        //given
        Long clubId = -1L;
        given(clubService.deleteClub(clubId)).willReturn(Optional.empty());

        //when
        MvcResult badIdResult = mockMvc.perform(
                        delete("/club/{clubId}", clubId)
                                .with(csrf())
                ).andExpect(status().isBadRequest())
                .andReturn();

        //then
        Assertions.assertThat(badIdResult.getResolvedException()).isExactlyInstanceOf(MissingAliveClubException.class);
     }

    @Test
    public void cancelClubDeletionById_Default_Success() throws Exception {
        //given
        given(clubService.reviveClub(0L)).willReturn(Optional.of("Test Club Name"));

        //when
        ResultActions actions = mockMvc.perform(
                delete("/club/{clubId}/cancel", 0L)
                        .with(csrf())
        );

        //then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(0L))
                .andExpect(jsonPath("$.name").value("Test Club Name"))
                .andDo(
                        document("club/revive/club"
                                , pathParameters(
                                        parameterWithName("clubId").description("살리려는 동아리 ID").attributes(example("1"))
                                ),
                                responseFields(
                                        fieldWithPath("id").type(WireFormat.FieldType.INT64).description("살아난 동아리 ID").attributes(example("1")),
                                        fieldWithPath("name").type(WireFormat.FieldType.STRING).description("살아난 동아리 이름").attributes(example("Test Club Name"))
                                )
                        )
                );
    }

    @Test
    public void cancelClubDeletionById_NoDeletedClubExist_MissingDeletedClubException() throws Exception {
        Long clubId = -1L;
        given(clubService.reviveClub(clubId)).willReturn(Optional.empty());

        //when
        MvcResult badIdResult = mockMvc.perform(
                        delete("/club/{clubId}/cancel", clubId)
                                .with(csrf())
                ).andExpect(status().isBadRequest())
                .andReturn();

        //then
        Assertions.assertThat(badIdResult.getResolvedException()).isExactlyInstanceOf(MissingDeletedClubException.class);
    }

    @Test
    public void deleteActivityImage_Default_Success() throws Exception {
        //given
        String testImageName = "Test Activity Image Name";
        long clubId = 0L;
        given(clubService.deleteActivityImage(clubId, testImageName)).willReturn(Optional.of(testImageName));

        //when
        ResultActions actions = mockMvc.perform(
                delete("/club/{clubId}/activityImage", clubId)
                        .with(csrf())
                        .queryParam("activityImageName", testImageName)
        );

        //then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.clubId").value(clubId))
                .andExpect(jsonPath("$.deletedActivityImageName").value(testImageName))
                .andDo(
                        document("club/delete/activityImage"
                                , pathParameters(
                                        parameterWithName("clubId").description("대상 동아리 ID").attributes(example("1"))
                                ),
                                queryParameters(
                                        parameterWithName("activityImageName").attributes(example("activity.png")).description("지우려는 활동 이미지 파일명")
                                ),
                                responseFields(
                                        fieldWithPath("clubId").type(WireFormat.FieldType.INT64).description("대상 동아리 ID").attributes(example("1")),
                                        fieldWithPath("deletedActivityImageName").type(WireFormat.FieldType.STRING).description("지워진 활동 사진 파일명").attributes(example("activity.png"))
                                )
                        )
                );

    }
       
    @Test
    public void deleteActivityImage_ClubIdAndImgNameMisMatch_ActivityImageMisMatchException() throws Exception{
        //given
        Long clubId = -1L;
        String activityImageName = "testImg.jpg";
        given(clubService.deleteActivityImage(clubId, activityImageName)).willReturn(Optional.empty());
        
        //when
        MvcResult imgIdMisMatchResult = mockMvc.perform(
                        delete("/club/{clubId}/activityImage", clubId)
                                .with(csrf())
                                .queryParam("activityImageName", activityImageName)
                ).andExpect(status().isBadRequest())
                .andReturn();

        //then
        Assertions.assertThat(imgIdMisMatchResult.getResolvedException()).isExactlyInstanceOf(ActivityImageMisMatchException.class);
        
     }
}