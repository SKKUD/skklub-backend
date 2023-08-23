package com.skklub.admin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skklub.admin.service.RefreshTokenService;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@Slf4j
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@ContextConfiguration
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@WebMvcTest(controllers = RefreshTokenController.class)
@MockBean(JpaMetamodelMappingContext.class)
public class RefreshTokenControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    RefreshTokenService refreshTokenService;

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
    @DisplayName("refreshToken")
    @WithMockUser
    public void refreshToken_Success() throws Exception {
        //given
        String accessToken =  "access_token";
        String refreshToken = "refresh_token";

        given(refreshTokenService.refreshAccessToken(any(),any())).willReturn(accessToken);

        //when
        ResultActions actions = mockMvc.perform(get("/refresh").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization","Bearer " + accessToken)
                .header("Refresh-Token","Bearer "+ refreshToken)
        );

        //then
        actions.andExpect(MockMvcResultMatchers.status().isNoContent())
                .andDo(print())
                .andDo(document("RefreshToken-Refresh"
                        ,responseHeaders(
                                headerWithName("Authorization").description("재발급된 access-token")
                        )
                ));
    }

}