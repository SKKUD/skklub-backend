package com.skklub.admin.service;

import com.skklub.admin.domain.Club;
import com.skklub.admin.domain.imagefile.Logo;
import com.skklub.admin.domain.PendingClub;
import com.skklub.admin.domain.User;
import com.skklub.admin.domain.enums.Campus;
import com.skklub.admin.domain.enums.ClubType;
import com.skklub.admin.domain.enums.Role;
import com.skklub.admin.repository.ClubRepository;
import com.skklub.admin.repository.PendingClubRepository;
import com.skklub.admin.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;

@Slf4j
@ExtendWith(MockitoExtension.class)
class PendingClubServiceTest {
    @InjectMocks
    private PendingClubService pendingClubService;
    @Mock
    private PendingClubRepository pendingClubRepository;
    @Mock
    private ClubRepository clubRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserService userService;

    @Test
    public void acceptRequest_Default_ReturnClubWithUser() throws Exception{
        //given
        PendingClub pendingClub = new PendingClub(
                "testPendingName",
                "testBriefDescription",
                "testActivityDescription",
                "testClubDescription",
                "testUserId",
                new BCryptPasswordEncoder().encode("testPw"),
                "testUser",
                "testContact",
                Role.ROLE_ADMIN_SEOUL_CENTRAL
        );
        Campus campus = Campus.명륜;
        ClubType clubType = ClubType.중앙동아리;
        String belongs = "종교";
        User user = pendingClub.toUser();
        Club club = pendingClub.toClubWithDefaultLogo(campus, clubType, belongs, user);
        Long pendingClubId = 41L;
        given(pendingClubRepository.findById(pendingClubId)).willReturn(Optional.of(pendingClub));
        doAnswer(invocation -> null).when(userRepository).save(user);
        doAnswer(invocation -> null).when(clubRepository).save(club);
        doNothing().when(pendingClubRepository).delete(pendingClub);

        //when
        Optional<Club> acceptedClub = pendingClubService.acceptRequest(pendingClubId, campus, clubType, belongs);

        //then
        Logo logo = club.getLogo();
        Assertions.assertThat(logo.getOriginalName()).isEqualTo("alt.jpg");
        Assertions.assertThat(logo.getUploadedName()).isEqualTo("alt.jpg");
        Assertions.assertThat(club.getName()).isEqualTo(pendingClub.getClubName());
        Assertions.assertThat(club.getBriefActivityDescription()).isEqualTo(pendingClub.getBriefActivityDescription());
        Assertions.assertThat(club.getActivityDescription()).isEqualTo(pendingClub.getActivityDescription());
        Assertions.assertThat(club.getClubDescription()).isEqualTo(pendingClub.getClubDescription());
        Assertions.assertThat(club.getCampus()).isEqualTo(campus);
        Assertions.assertThat(club.getClubType()).isEqualTo(clubType);
        Assertions.assertThat(club.getBelongs()).isEqualTo(belongs);
        Assertions.assertThat(club.getHeadLine()).isNull();
        Assertions.assertThat(club.getEstablishAt()).isNull();
        Assertions.assertThat(club.getRoomLocation()).isNull();
        Assertions.assertThat(club.getMemberAmount()).isNull();
        Assertions.assertThat(club.getRegularMeetingTime()).isNull();
        Assertions.assertThat(club.getMandatoryActivatePeriod()).isNull();
        Assertions.assertThat(club.getWebLink1()).isNull();
        Assertions.assertThat(club.getWebLink2()).isNull();
        Assertions.assertThat(club.getRecruit()).isNull();
        Assertions.assertThat(club.getActivityImages()).isEmpty();
        Assertions.assertThat(club.getPresident()).isEqualTo(user);
    }
}