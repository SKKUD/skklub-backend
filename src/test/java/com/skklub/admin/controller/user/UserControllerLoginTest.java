package com.skklub.admin.controller.user;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.skklub.admin.controller.AuthValidator;
import com.skklub.admin.controller.UserController;
import com.skklub.admin.domain.enums.Role;
import com.skklub.admin.repository.UserRepository;
import com.skklub.admin.service.UserService;
import com.skklub.admin.service.dto.UserLoginDTO;
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
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@Slf4j
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@WebMvcTest(controllers = UserController.class)
@MockBean(JpaMetamodelMappingContext.class)
public class UserControllerLoginTest {

    @Autowired
    MockMvc mockMvc;
    @MockBean
    UserService userService;
    @MockBean
    UserRepository userRepository;
    @MockBean
    private AuthValidator authValidator;
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    public void mockMvcSetUp(RestDocumentationContextProvider restDocumentation) {
        mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).apply(springSecurity()).apply(documentationConfiguration(restDocumentation)).build();
    }

    @AfterEach
    public void afterEach() throws Exception {
        mockMvc = null;
    }

    @Test
    @DisplayName("Login_Success_Test")
    @WithMockUser
    public void login_Success() throws Exception {

        //given
        String username = "user";
        String password = "1234";
        UserLoginDTO userLoginDTO = new UserLoginDTO(0L,username,Role.ROLE_USER,"access","refresh");
        given(userService.loginUser(username, password)).willReturn(userLoginDTO);

        //when
        ResultActions actions = mockMvc.perform(post("/user/login").with(csrf()).contentType(MediaType.APPLICATION_JSON).queryParam("username", username).queryParam("password", password));

        //then
        actions.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.id").value(userLoginDTO.getId()))
                .andExpect(jsonPath("$.username").value(userLoginDTO.getUsername()))
                .andExpect(jsonPath("$.role").value(String.valueOf(userLoginDTO.getRole())))
                .andDo(document("User-Login", queryParameters(parameterWithName("username").description("유저 계정 ID").attributes(example(username)), parameterWithName("password").description("비밀번호").attributes(example(password))), responseHeaders(headerWithName("Authorization").description("기본 인증용 access-token"), headerWithName("Refresh-Token").description("access-token 재발급용 refresh-token"))

        ));
    }

}
