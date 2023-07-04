package com.skklub.admin.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.test.context.support.WithMockUser;

import static org.junit.jupiter.api.Assertions.*;


@Slf4j
@WithMockUser
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@WebMvcTest(controllers = NoticeController.class)
@MockBean(JpaMetamodelMappingContext.class)
class NoticeControllerTest {

}