package com.skklub.admin.integration;

import com.skklub.admin.TestDataRepository;
import com.skklub.admin.WithMockCustomUser;
import com.skklub.admin.controller.ClubController;
import com.skklub.admin.controller.dto.ClubCreateRequestDTO;
import com.skklub.admin.controller.dto.ClubNameAndIdDTO;
import com.skklub.admin.domain.Club;
import com.skklub.admin.domain.enums.Role;
import com.skklub.admin.repository.ClubRepository;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Slf4j
@SpringBootTest
@Transactional
@Import(TestDataRepository.class)

public class AuditorAwareIntegrationTest {
    @Autowired
    private ClubController clubController;
    @Autowired
    private ClubRepository clubRepository;
    @Autowired
    private TestDataRepository testDataRepository;

    @Test
    @DisplayName("userAuditorAware_create")
    @Transactional
    @WithMockCustomUser(username = "tester",password = "tester_pw",role = Role.ROLE_MASTER)
    public void userAuditorAware_WithMockSecurityContext() throws Exception{

        //given
        ClubCreateRequestDTO clubCreateRequestDTO = testDataRepository.getClubCreateRequestDTO();
        Path path = Paths.get("src/test/resources/img/1.jpg");
        byte[] bytes = Files.readAllBytes(path);
        String logoName = "1.jpg";
        MockMultipartFile logo = new MockMultipartFile("logo", logoName, "image/jpeg", bytes);

        //when
        ClubNameAndIdDTO clubNameAndIdDTO = clubController.createClub(clubCreateRequestDTO, logo);
        Optional<Club> club = clubRepository.findById(clubNameAndIdDTO.getId());

        //then
        Assertions.assertThat(club.get().getCreatedBy()).isEqualTo("tester");
        Assertions.assertThat(club.get().getLastModifiedBy()).isEqualTo("tester");

    }

}
