package com.skklub.admin.club.unit;

import com.skklub.admin.controller.AuthValidator;
import com.skklub.admin.controller.ClubController;
import com.skklub.admin.controller.S3Transferer;
import com.skklub.admin.controller.dto.news.ClubFullInformationDTO;
import com.skklub.admin.controller.dto.news.ClubMetaDTO;
import com.skklub.admin.controller.dto.news.ClubOperationDTO;
import com.skklub.admin.controller.dto.news.UserPublicInformationDTO;
import com.skklub.admin.domain.Club;
import com.skklub.admin.domain.ClubOperation;
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
    public void getFullClubByOperationId_Default_Success() throws Exception{
        //given
        Long clubOperationId = 123L;
        ClubMetaDTO clubMetaDTO = ClubMetaDTO.builder()
                .name("testClub")
                .description("testClubDescription testClubDescription testClubDescription testClubDescription testClubDescription testClubDescription testClubDescription testClubDescription testClubDescription testClubDescription")
                .activityDescription("테스트 클럽 활동 설명입니다. 테스트 클럽 활동 설명입니다. 테스트 클럽 활동 설명입니다. 테스트 클럽 활동 설명입니다. 테스트 클럽 활동 설명입니다. 테스트 클럽 활동 설명입니다. ")
                .build();
        ClubOperationDTO clubOperationDTO = ClubOperationDTO
        UserPublicInformationDTO userPublicInformationDTO = UserPublicInformationDTO.builder()
                .name("테스트 유저 이름")
                .contact("010-1234-1234")
                .build();

        ClubFullInformationDTO clubFullInformationDTO = ClubFullInformationDTO.builder()
                .club_operation_id(clubOperationId)
                .clubMetaDTO(clubMetaDTO)
                .userPublicInformationDTO(userPublicInformationDTO)
                .build();
        ClubOperation clubOperation = ClubOperation.builder()
                .id(clubOperationId)
                .headLine("테스트 클럽 한줄 설명")
                .mandatoryActivatePeriod("테스트 클럽 의무 활동 기간")
                .memberAmount(60) //테스트 클럽 정원
                .regularMeetingTime("테스트 클럽 정규 모임 시간")
                .roomLocation("테스트")
                .build();

        //mocking
        clubService.findClubFullInformationByClubOperationId(clubOperationId);

        //when


        //then

    }
}
