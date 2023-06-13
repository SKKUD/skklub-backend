package com.skklub.admin.controller.user;


import akka.protobuf.WireFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skklub.admin.controller.UserController;
import com.skklub.admin.service.UserService;
import com.skklub.admin.service.dto.UserProcResultDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static com.skklub.admin.controller.RestDocsUtils.example;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@Slf4j
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@WebMvcTest(controllers = UserController.class)
@MockBean(JpaMetamodelMappingContext.class)
public class UserControllerJoinTest {
    @Autowired
    MockMvc mockMvc;
    @MockBean
    UserService userService;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    public void mockMvcSetUp(
    RestDocumentationContextProvider restDocumentation) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(this.webApplicationContext)
                .apply(springSecurity())
                .apply(documentationConfiguration(restDocumentation))
                .build();
    }

    @AfterEach
    public void afterEach() throws Exception {
        mockMvc = null;
    }

    @Test
    @DisplayName("Join_Success_Test")
    @WithMockUser
    public void join_Success() throws Exception {

        //given
        given(userService.userJoin(any())).willReturn(new UserProcResultDTO(1L,"user","김명륜","010-1234-5678"));

        //when
        ResultActions actions = mockMvc.perform(post("/user/join").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                        .queryParam("username", "user")
                        .queryParam("password", "1234")
                        .queryParam("name", "김명륜")
                        .queryParam("contact", "010-1234-5678")
                );

        //then
        actions.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("user"))
                .andExpect(jsonPath("$.name").value("김명륜"))
                .andExpect(jsonPath("$.contact").value("010-1234-5678"))
                .andDo(print())
                .andDo(document("User-Join"
                        ,queryParameters(
                                parameterWithName("username").description("유저 계정 ID").attributes(example("user")),
                                parameterWithName("password").description("비밀번호").attributes(example("1234")),
                                parameterWithName("name").description("유저 이름").attributes(example("김명륜")),
                                parameterWithName("contact").description("연락처").attributes(example("010-1234-5678")))

                        ,responseFields(
                                fieldWithPath("id").type(WireFormat.FieldType.INT64).description("유저 ID"),
                                fieldWithPath("username").type(WireFormat.FieldType.STRING).description("유저 계정 ID"),
                                fieldWithPath("name").type(WireFormat.FieldType.STRING).description("유저 이름"),
                                fieldWithPath("contact").type(WireFormat.FieldType.STRING).description("연락처")
                        )
                ));
    }

}