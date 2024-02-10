package com.skklub.admin.deprecated.integration.pendingClub;

import com.skklub.admin.WithMockCustomUser;
import com.skklub.admin.controller.PendingClubController;
import com.skklub.admin.controller.dto.ClubCreateResponse;
import com.skklub.admin.controller.dto.PendingClubRequest;
import com.skklub.admin.controller.dto.PendingInformationResponse;
import com.skklub.admin.domain.Club;
import com.skklub.admin.domain.Logo;
import com.skklub.admin.domain.PendingClub;
import com.skklub.admin.domain.User;
import com.skklub.admin.domain.enums.Campus;
import com.skklub.admin.domain.enums.ClubType;
import com.skklub.admin.domain.enums.Role;
import com.skklub.admin.repository.ClubRepository;
import com.skklub.admin.repository.LogoRepository;
import com.skklub.admin.repository.PendingClubRepository;
import com.skklub.admin.repository.UserRepository;
import com.skklub.admin.security.auth.PrincipalDetailsService;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Transactional
@SpringBootTest
@WithMockCustomUser(username = "testMasterID",role = Role.ROLE_MASTER)
public class PendingClubIntegrationTest {
    @Autowired
    private PendingClubController pendingClubController;
    @Autowired
    private PendingClubRepository pendingClubRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ClubRepository clubRepository;
    @Autowired
    private EntityManager em;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private PrincipalDetailsService principalDetailsService;
    @Autowired
    private LogoRepository logoRepository;

    @Test
    public void createPending_ToAdminSeoul_SaveWell() throws Exception {
        //given
        Role role = Role.ROLE_ADMIN_SEOUL_CENTRAL;
        String password = "insertPendingTestPassword";
        PendingClubRequest pendingClubRequest = PendingClubRequest.builder()
                .requestTo(role)
                .clubName("insertPendingTestName")
                .briefActivityDescription("insertPendingTestBriefDescription")
                .clubDescription("insertPendingTestClubDescription")
                .activityDescription("insertPendingTestActivityDescription")
                .username("insertPendingTestUserName")
                .password(password)
                .presidentName("insertPendingTestPresidentName")
                .presidentContact("insertPendingTestContact")
                .build();

        //when
        PendingInformationResponse response = pendingClubController.createPending(pendingClubRequest);
        em.flush();
        em.clear();

        //then
        Optional<PendingClub> pendingClub = pendingClubRepository.findById(response.getPendingClubId());
        Assertions.assertThat(pendingClub).isNotEmpty();
        Assertions.assertThat(pendingClub.get().getRequestTo()).isEqualTo(pendingClubRequest.getRequestTo());
        Assertions.assertThat(pendingClub.get().getClubName()).isEqualTo(pendingClubRequest.getClubName());
        Assertions.assertThat(pendingClub.get().getBriefActivityDescription()).isEqualTo(pendingClubRequest.getBriefActivityDescription());
        Assertions.assertThat(pendingClub.get().getClubDescription()).isEqualTo(pendingClubRequest.getClubDescription());
        Assertions.assertThat(pendingClub.get().getActivityDescription()).isEqualTo(pendingClubRequest.getActivityDescription());
        Assertions.assertThat(pendingClub.get().getUsername()).isEqualTo(pendingClubRequest.getUsername());
        Assertions.assertThat(bCryptPasswordEncoder.matches(password, pendingClub.get().getPassword())).isTrue();
        Assertions.assertThat(pendingClub.get().getPresidentName()).isEqualTo(pendingClubRequest.getPresidentName());

        Assertions.assertThat(response.getRequestTo()).isEqualTo(pendingClubRequest.getRequestTo());
        Assertions.assertThat(response.getClubName()).isEqualTo(pendingClubRequest.getClubName());
        Assertions.assertThat(response.getBriefActivityDescription()).isEqualTo(pendingClubRequest.getBriefActivityDescription());
        Assertions.assertThat(response.getClubDescription()).isEqualTo(pendingClubRequest.getClubDescription());
        Assertions.assertThat(response.getActivityDescription()).isEqualTo(pendingClubRequest.getActivityDescription());
        Assertions.assertThat(response.getPresidentName()).isEqualTo(pendingClubRequest.getPresidentName());
        Assertions.assertThat(response.getPresidentContact()).isEqualTo(pendingClubRequest.getPresidentContact());

    }

    @Test
    public void createPending_ToMaster_SaveWell() throws Exception {
        //given
        Role role = Role.ROLE_MASTER;
        String password = "insertPendingTestPassword";
        String encodedPw = bCryptPasswordEncoder.encode(password);
        PendingClubRequest pendingClubRequest = PendingClubRequest.builder()
                .requestTo(role)
                .clubName("insertPendingTestName")
                .briefActivityDescription("insertPendingTestBriefDescription")
                .clubDescription("insertPendingTestClubDescription")
                .activityDescription("insertPendingTestActivityDescription")
                .username("insertPendingTestUserName")
                .password(password)
                .presidentName("insertPendingTestPresidentName")
                .presidentContact("insertPendingTestContact")
                .build();

        //when
        PendingInformationResponse response = pendingClubController.createPending(pendingClubRequest);
        em.flush();
        em.clear();

        //then
        Optional<PendingClub> pendingClub = pendingClubRepository.findById(response.getPendingClubId());
        Assertions.assertThat(pendingClub).isNotEmpty();
        Assertions.assertThat(pendingClub.get().getRequestTo()).isEqualTo(pendingClubRequest.getRequestTo());
        Assertions.assertThat(pendingClub.get().getClubName()).isEqualTo(pendingClubRequest.getClubName());
        Assertions.assertThat(pendingClub.get().getBriefActivityDescription()).isEqualTo(pendingClubRequest.getBriefActivityDescription());
        Assertions.assertThat(pendingClub.get().getClubDescription()).isEqualTo(pendingClubRequest.getClubDescription());
        Assertions.assertThat(pendingClub.get().getActivityDescription()).isEqualTo(pendingClubRequest.getActivityDescription());
        Assertions.assertThat(pendingClub.get().getUsername()).isEqualTo(pendingClubRequest.getUsername());
        Assertions.assertThat(bCryptPasswordEncoder.matches(password, pendingClub.get().getPassword())).isTrue();
        Assertions.assertThat(pendingClub.get().getPresidentName()).isEqualTo(pendingClubRequest.getPresidentName());

        Assertions.assertThat(response.getRequestTo()).isEqualTo(pendingClubRequest.getRequestTo());
        Assertions.assertThat(response.getClubName()).isEqualTo(pendingClubRequest.getClubName());
        Assertions.assertThat(response.getBriefActivityDescription()).isEqualTo(pendingClubRequest.getBriefActivityDescription());
        Assertions.assertThat(response.getClubDescription()).isEqualTo(pendingClubRequest.getClubDescription());
        Assertions.assertThat(response.getActivityDescription()).isEqualTo(pendingClubRequest.getActivityDescription());
        Assertions.assertThat(response.getPresidentName()).isEqualTo(pendingClubRequest.getPresidentName());
        Assertions.assertThat(response.getPresidentContact()).isEqualTo(pendingClubRequest.getPresidentContact());

    }

    @Test
    @WithMockCustomUser(username = "testAdminID1")
    public void getPendings_LoginWithSuwonCentralAdmin_OnlyIncludeSuwonAdmin() throws Exception {
        //given
        Role role = Role.ROLE_ADMIN_SUWON_CENTRAL;
        PageRequest pageRequest = PageRequest.of(1, 3, Sort.by("clubName").ascending());
        Page<PendingInformationResponse> pendingClubs = pendingClubRepository.findAllByRequestTo(role, pageRequest)
                .map(PendingInformationResponse::new);
        em.flush();
        em.clear();
        UserDetails userDetails = principalDetailsService.loadUserByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

        //when
        Page<PendingInformationResponse> response = pendingClubController.getPendings(userDetails, PageRequest.of(1, 3));

        //then
        Assertions.assertThat(response.getContent()).isNotEmpty();
        Assertions.assertThat(response.getSort()).isEqualTo(Sort.by("clubName").ascending());
        Assertions.assertThat(response.getTotalPages()).isEqualTo(pendingClubs.getTotalPages());
        Assertions.assertThat(response.getTotalElements()).isEqualTo(pendingClubs.getTotalElements());
        Assertions.assertThat(response.getSize()).isEqualTo(pendingClubs.getSize());
        Assertions.assertThat(response.getNumber()).isEqualTo(pendingClubs.getNumber());
        Assertions.assertThat(response.getNumberOfElements()).isEqualTo(pendingClubs.getNumberOfElements());
        response.getContent().stream()
                .forEach(pendingInformationResponse ->
                        {
                            Assertions.assertThat(pendingClubs.getContent().contains(pendingInformationResponse)).isTrue();
                            Assertions.assertThat(pendingInformationResponse.getRequestTo())
                                    .isEqualTo(role);
                        }
                );
    }

    @Test
    @WithMockCustomUser(username = "testMasterID")
    public void getPendings_LoginWithMaster_OnlyIncludeMaster() throws Exception {
        //given
        Role role = Role.ROLE_MASTER;
        PageRequest pageRequest = PageRequest.of(1, 3, Sort.by("clubName").ascending());
        Page<PendingInformationResponse> pendingClubs = pendingClubRepository.findAllByRequestTo(role, pageRequest)
                .map(PendingInformationResponse::new);
        em.flush();
        em.clear();
        UserDetails userDetails = principalDetailsService.loadUserByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

        //when
        Page<PendingInformationResponse> response = pendingClubController.getPendings(userDetails, PageRequest.of(1, 3));

        //then
        Assertions.assertThat(response.getContent()).isNotEmpty();
        Assertions.assertThat(response.getSort()).isEqualTo(Sort.by("clubName").ascending());
        Assertions.assertThat(response.getTotalPages()).isEqualTo(pendingClubs.getTotalPages());
        Assertions.assertThat(response.getTotalElements()).isEqualTo(pendingClubs.getTotalElements());
        Assertions.assertThat(response.getSize()).isEqualTo(pendingClubs.getSize());
        Assertions.assertThat(response.getNumber()).isEqualTo(pendingClubs.getNumber());
        Assertions.assertThat(response.getNumberOfElements()).isEqualTo(pendingClubs.getNumberOfElements());
        response.getContent().stream()
                .forEach(pendingInformationResponse ->
                        {
                            Assertions.assertThat(pendingClubs.getContent().contains(pendingInformationResponse)).isTrue();
                            Assertions.assertThat(pendingInformationResponse.getRequestTo())
                                    .isEqualTo(role);
                        }
                );

    }

    @Test
    @WithMockCustomUser(username = "testAdminID0", role = Role.ROLE_ADMIN_SEOUL_CENTRAL)
    public void acceptPending_GivenPendingClub_SaveClubWithDefaultLogoAndUserWellAndDeletePendingClub() throws Exception {
        //given
        PendingClub pendingClub = em.createQuery("select p from PendingClub p", PendingClub.class)
                .setMaxResults(1)
                .getSingleResult();
        em.clear();
        Campus campus = Campus.명륜;
        ClubType clubType = ClubType.중앙동아리;
        String belongs = "종교";

        //when
        ClubCreateResponse response = pendingClubController.acceptPending(pendingClub.getId(), campus, clubType, belongs);
        em.flush();
        em.clear();

        //then
        //Deletion
        Assertions.assertThat(pendingClubRepository.findById(pendingClub.getId())).isEmpty();

        //Club Creation
        Optional<Club> club = clubRepository.findById(response.getClubId());
        Assertions.assertThat(club).isNotEmpty();
        Assertions.assertThat(club.get().getCampus()).isEqualTo(campus);
        Assertions.assertThat(club.get().getClubType()).isEqualTo(clubType);
        Assertions.assertThat(club.get().getBelongs()).isEqualTo(belongs);
        Assertions.assertThat(club.get().getName()).isEqualTo(pendingClub.getClubName());
        Assertions.assertThat(club.get().getBriefActivityDescription()).isEqualTo(pendingClub.getBriefActivityDescription());
        Assertions.assertThat(club.get().getActivityDescription()).isEqualTo(pendingClub.getActivityDescription());
        Assertions.assertThat(club.get().getClubDescription()).isEqualTo(pendingClub.getClubDescription());
        Assertions.assertThat(club.get().getHeadLine()).isNull();
        Assertions.assertThat(club.get().getEstablishAt()).isNull();
        Assertions.assertThat(club.get().getRoomLocation()).isNull();
        Assertions.assertThat(club.get().getMemberAmount()).isNull();
        Assertions.assertThat(club.get().getRegularMeetingTime()).isNull();
        Assertions.assertThat(club.get().getMandatoryActivatePeriod()).isNull();
        Assertions.assertThat(club.get().getWebLink1()).isNull();
        Assertions.assertThat(club.get().getWebLink2()).isNull();
        Assertions.assertThat(club.get().getRecruit()).isNull();
        Assertions.assertThat(club.get().getActivityImages()).isEmpty();

        Logo logo = club.get().getLogo();
        Assertions.assertThat(logo.getUploadedName()).isEqualTo("alt.jpg");
        Assertions.assertThat(logo.getOriginalName()).isEqualTo("alt.jpg");
        Assertions.assertThat(logoRepository.existsById(logo.getId())).isTrue();

        //User Creation
        User user = userRepository.findByUsername(response.getUsername());
        Assertions.assertThat(user).isEqualTo(club.get().getPresident()); //Foreign Relation
        Assertions.assertThat(user.getRole()).isEqualTo(Role.ROLE_USER);
        Assertions.assertThat(user.getName()).isEqualTo(pendingClub.getPresidentName());
        Assertions.assertThat(user.getContact()).isEqualTo(pendingClub.getPresidentContact());
    }

    @Test
    @WithMockCustomUser(username = "testAdminID0", role = Role.ROLE_ADMIN_SEOUL_CENTRAL)
    public void denyPending_GivenPendingClub_DeletePendingClub() throws Exception {
        //given
        PendingClub pendingClub = em.createQuery("select p from PendingClub p", PendingClub.class)
                .setMaxResults(1)
                .getSingleResult();
        long clubCnt = clubRepository.count();
        long logoCnt = logoRepository.count();
        long userCnt = userRepository.count();
        em.clear();

        //when
        PendingInformationResponse response = pendingClubController.denyPending(pendingClub.getId());
        em.flush();
        em.clear();

        //then
        Assertions.assertThat(clubCnt).isEqualTo(clubRepository.count());
        Assertions.assertThat(logoCnt).isEqualTo(logoRepository.count());
        Assertions.assertThat(userCnt).isEqualTo(userRepository.count());
        Assertions.assertThat(pendingClubRepository.existsById(pendingClub.getId())).isFalse();

        Assertions.assertThat(response.getPendingClubId()).isEqualTo(pendingClub.getId());
        Assertions.assertThat(response.getRequestTo()).isEqualTo(pendingClub.getRequestTo());
        Assertions.assertThat(response.getRequestedAt()).isEqualTo(pendingClub.getCreatedAt());
        Assertions.assertThat(response.getClubName()).isEqualTo(pendingClub.getClubName());
        Assertions.assertThat(response.getBriefActivityDescription()).isEqualTo(pendingClub.getBriefActivityDescription());
        Assertions.assertThat(response.getActivityDescription()).isEqualTo(pendingClub.getActivityDescription());
        Assertions.assertThat(response.getClubDescription()).isEqualTo(pendingClub.getClubDescription());
        Assertions.assertThat(response.getPresidentName()).isEqualTo(pendingClub.getPresidentName());
        Assertions.assertThat(response.getPresidentContact()).isEqualTo(pendingClub.getPresidentContact());

    }

}
