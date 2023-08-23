package com.skklub.admin.integration;

import com.skklub.admin.TestDataRepository;
import com.skklub.admin.WithMockCustomUser;
import com.skklub.admin.controller.PendingClubController;
import com.skklub.admin.controller.dto.PendingClubRequest;
import com.skklub.admin.domain.PendingClub;
import com.skklub.admin.domain.enums.Role;
import com.skklub.admin.repository.PendingClubRepository;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Transactional
@ExtendWith(MockitoExtension.class)
@Import(TestDataRepository.class)
@SpringBootTest
public class AuditorAwareIntegrationTest {

    @Autowired
    private PendingClubController pendingClubController;
    @Autowired
    private PendingClubRepository pendingClubRepository;


    @Test
    @DisplayName("userAuditorAware_create")
    @Transactional
    @WithMockCustomUser(username = "userId0",password = "password",role = Role.ROLE_USER, name = "presidentName")
    public void userAuditorAware_WithMockSecurityContext() throws Exception{
        //given
        pendingClubController.createPending(new PendingClubRequest(Role.ROLE_ADMIN_SEOUL_CENTRAL,"TuNA","간략설명없음","상세설명없음","동아리설명없음","userId0","password","presidentName","contact"));
        //when

        Optional<PendingClub> pendingClub = pendingClubRepository.findById(0L);
        System.out.println(pendingClub);

        //then
        Assertions.assertThat(pendingClub.get().getCreatedAt()).isEqualTo("username");
        Assertions.assertThat(pendingClub.get().getLastModifiedAt()).isEqualTo("username");
    }

}
