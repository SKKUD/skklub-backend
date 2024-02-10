package com.skklub.admin.deprecated.integration;

import com.skklub.admin.TestDataRepository;
import com.skklub.admin.WithMockCustomUser;
import com.skklub.admin.controller.PendingClubController;
import com.skklub.admin.controller.dto.ClubUpdateRequest;
import com.skklub.admin.controller.dto.PendingClubRequest;
import com.skklub.admin.domain.Club;
import com.skklub.admin.domain.enums.Campus;
import com.skklub.admin.domain.enums.ClubType;
import com.skklub.admin.domain.enums.Role;
import com.skklub.admin.service.ClubService;
import com.skklub.admin.service.PendingClubService;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    PendingClubService pendingClubService;
    @Autowired
    ClubService clubService;

    @Test
    @DisplayName("userAuditorAware_createdAt")
    @WithMockCustomUser(username = "userId0",password = "password",role = Role.ROLE_USER, name = "presidentName")
    public void userAuditorAware_WithMockSecurityContext() throws Exception{
        //given
        Long pendingClubId = pendingClubController.createPending(
                new PendingClubRequest(Role.ROLE_ADMIN_SEOUL_CENTRAL,"TuNA","간략설명없음","상세설명없음","동아리설명없음","username","password","presidentName","contact"))
                .getPendingClubId();
        Club clubUpdateInfo = new ClubUpdateRequest("clubName","activityDescription","briefAD","clubDescription",2023,"headline","madatoryAP",0,"regularMeetingTime","roomLocation","webLink1","webLink2").toEntity();

        //when
        //1. created
        Optional<Club> club = pendingClubService.acceptRequest(pendingClubId, Campus.명륜, ClubType.소모임,"예술");
        LocalDateTime timeStamp1 = LocalDateTime.now();
        //2. modified
        Long clubId = club.get().getId();
        clubService.updateClub(clubId,clubUpdateInfo);
        LocalDateTime timeStamp2 = LocalDateTime.now();

        //then
        String format = "yyyy-mm-dd-HH:mm:ss:S";
        String realCreated = club.get().getCreatedAt().format(DateTimeFormatter.ofPattern(format));
        String realModified = club.get().getLastModifiedAt().format(DateTimeFormatter.ofPattern(format));
        String compareCreated=timeStamp1.format(DateTimeFormatter.ofPattern(format));
        String compareModified=timeStamp2.format(DateTimeFormatter.ofPattern(format));

        Assertions.assertThat(realCreated).isEqualTo(compareCreated);
        Assertions.assertThat(club.get().getCreatedBy()).isEqualTo("userId0");
        Assertions.assertThat(realModified).isEqualTo(compareModified);
        Assertions.assertThat(club.get().getLastModifiedBy()).isEqualTo("userId0");
    }

}
