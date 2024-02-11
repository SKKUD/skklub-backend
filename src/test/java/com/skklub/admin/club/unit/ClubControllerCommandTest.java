package com.skklub.admin.club.unit;

import com.skklub.admin.controller.AuthValidator;
import com.skklub.admin.controller.ClubController;
import com.skklub.admin.controller.S3Transferer;
import com.skklub.admin.domain.Club;
import com.skklub.admin.repository.ClubRepository;
import com.skklub.admin.repository.UserRepository;
import com.skklub.admin.service.ClubService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.web.servlet.MockMvc;

@Slf4j
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@MockBean(JpaMetamodelMappingContext.class)
@WebMvcTest(controllers = ClubController.class)
public class ClubControllerCommandTest {
    @MockBean
    private UserRepository userRepository;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ClubService clubService;
    @MockBean
    private ClubRepository clubRepository;
    @MockBean
    private S3Transferer s3Transferer;
    @MockBean
    private AuthValidator authValidator;

    /**
     * input :
     * expect result :
     */
    @Test
    @DisplayName("Club Id를 이용한 단일 상세 조회 - 정상 흐름")
    public void getClubById_Default_Success() throws Exception{
        //given
        Long clubId = 123L;
        new Club()

        //mocking

        //when


        //then

    }
}
