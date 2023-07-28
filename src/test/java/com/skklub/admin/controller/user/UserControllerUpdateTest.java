package com.skklub.admin.controller.user;

import akka.protobuf.WireFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skklub.admin.TestDataRepository;
import com.skklub.admin.controller.AuthValidator;
import com.skklub.admin.controller.UserController;
import com.skklub.admin.domain.User;
import com.skklub.admin.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Optional;

import static com.skklub.admin.controller.RestDocsUtils.example;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@Slf4j
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@WebMvcTest(controllers = UserController.class)
@MockBean(JpaMetamodelMappingContext.class)
public class UserControllerUpdateTest {

    @Autowired
    MockMvc mockMvc;
    @MockBean
    UserService userService;
    @MockBean
    private AuthValidator authValidator;
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @InjectMocks
    private TestDataRepository testDataRepository;

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
    @DisplayName("update_success")
    @WithMockUser
    public void update_success() throws Exception {

        Long userId = 0L;
        Long changeToId = 1L;
        User user = testDataRepository.getUsers().get(userId.intValue());
        User changeTo = testDataRepository.getUsers().get(changeToId.intValue());

        //given
        given(userService.updateUser(eq(userId),eq(changeTo.getPassword()),eq(changeTo.getRole()),eq(changeTo.getName()),eq(changeTo.getContact()),eq("Bearer (access_token)")))
                .willReturn(Optional.of(new User(user.getUsername(),changeTo.getPassword(),changeTo.getRole(),changeTo.getName(),changeTo.getContact())));


        //when
        ResultActions actions = mockMvc.perform(RestDocumentationRequestBuilders.post("/user/{userId}",userId)
                        .header("Authorization", "Bearer (access_token)")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                        .queryParam("password", changeTo.getPassword())
                        .queryParam("name", changeTo.getName())
                        .queryParam("contact", changeTo.getContact())
                );

        //then
        actions.andExpect(MockMvcResultMatchers.status().isOk())
                //.andExpect(jsonPath("$.id").value(0L))
                .andExpect(jsonPath("$.username").value(user.getUsername()))
                .andExpect(jsonPath("$.name").value(changeTo.getName()))
                .andExpect(jsonPath("$.contact").value(changeTo.getContact()))
                .andDo(print())
                .andDo(document("User-Update"
                        ,requestHeaders(headerWithName("Authorization").description("기본 인증용 access-token")
                        )
                        ,pathParameters(
                                parameterWithName("userId").description("유저 ID").attributes(example(String.valueOf(0L)))
                        )
                        ,queryParameters(
                                parameterWithName("password").description("변경할 비밀번호").attributes(example(changeTo.getPassword())),
                                parameterWithName("name").description("변경할 유저 이름").attributes(example(changeTo.getName())),
                                parameterWithName("contact").description("변경할 연락처").attributes(example(changeTo.getContact())))
                        ,responseFields(
                                fieldWithPath("id").type(WireFormat.FieldType.INT64).description("유저 ID"),
                                fieldWithPath("username").type(WireFormat.FieldType.STRING).description("유저 계정 ID"),
                                fieldWithPath("name").type(WireFormat.FieldType.STRING).description("유저 이름"),
                                fieldWithPath("contact").type(WireFormat.FieldType.STRING).description("연락처")
                        )
                ));
    }

}
