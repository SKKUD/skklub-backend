package com.skklub.admin.controller.club;

import akka.protobuf.WireFormat;
import com.skklub.admin.controller.ClubController;
import com.skklub.admin.controller.S3Transferer;
import com.skklub.admin.service.ClubService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.conn.Wire;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.restdocs.operation.QueryParameters;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Optional;

import static com.skklub.admin.controller.RestDocsUtils.example;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@WebMvcTest(controllers = ClubController.class)
@MockBean(JpaMetamodelMappingContext.class)
class ClubControllerDeleteTest {
    @MockBean
    private ClubService clubService;

    @MockBean
    private S3Transferer s3Transferer;

    @Autowired
    private MockMvc mockMvc;


    @Test
    public void deleteClubById_Default_Success() throws Exception{
        //given
        given(clubService.deleteClub(0L)).willReturn(Optional.of("Test Club Name"));

        //when
        ResultActions actions = mockMvc.perform(
                delete("/club/{clubId}", 0L)
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
                                        fieldWithPath("id").type(WireFormat.FieldType.INT64).description("삭제된 동아리 ID"),
                                        fieldWithPath("name").type(WireFormat.FieldType.STRING).description("삭제된 동아리 이름")
                                )
                        )
                );
     }

     @Test
     public void cancelClubDeletionById_Default_Success() throws Exception{
         //given
         given(clubService.reviveClub(0L)).willReturn(Optional.of("Test Club Name"));

         //when
         ResultActions actions = mockMvc.perform(
                 delete("/club/{clubId}/cancel", 0L)
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
                                         fieldWithPath("id").type(WireFormat.FieldType.INT64).description("살아난 동아리 ID"),
                                         fieldWithPath("name").type(WireFormat.FieldType.STRING).description("살아난 동아리 이름")
                                 )
                         )
                 );
      }

      @Test
      public void deleteActivityImage_Default_Success() throws Exception{
          //given
          String testImageName = "Test Activity Image Name";
          long clubId = 0L;
          given(clubService.deleteActivityImage(clubId, testImageName)).willReturn(Optional.of(testImageName));

          //when
          ResultActions actions = mockMvc.perform(
                  delete("/club/{clubId}/activityImage", clubId)
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
                                          fieldWithPath("clubId").type(WireFormat.FieldType.INT64).description("대상 동아리 ID"),
                                          fieldWithPath("deletedActivityImageName").type(WireFormat.FieldType.STRING).description("지워진 활동 사진 파일명")
                                  )
                          )
                  );

       }
}