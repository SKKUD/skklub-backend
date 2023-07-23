package com.skklub.admin.repository;

import com.skklub.admin.domain.PendingClub;
import com.skklub.admin.domain.enums.Role;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PendingClubRepositoryTest {
    @Autowired
    private PendingClubRepository pendingClubRepository;
    @Autowired
    private ClubRepository clubRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EntityManager em;
    private final int pendingClubCnt = 21;
    private final List<PendingClub> pendingClubs = new ArrayList<>();

    @BeforeAll
    public void beforeAll() {
        for (int i = 0; i < pendingClubCnt; i++) {
            Role reqTo;
            switch (i % 3) {
                case 0 -> reqTo = Role.ROLE_ADMIN_SEOUL_CENTRAL;
                case 1 -> reqTo = Role.ROLE_ADMIN_SUWON_CENTRAL;
                default -> reqTo = Role.ROLE_MASTER;
            }
            pendingClubs.add(
                    new PendingClub(
                            "testPendingName" + i,
                            "testBriefDescription" + i,
                            "testActivityDescription" + i,
                            "testClubDescription" + i,
                            "testUserId" + i,
                            "testPw" + i,
                            "testUser" + i,
                            "testContact" + i,
                            reqTo
                    )
            );
        }
        pendingClubRepository.saveAll(pendingClubs);
    }

    @Test
    public void findAllByRequestTo_SearchByRoleAdminSeoulCentral_Success() throws Exception {
        //given
        Role reqTo = Role.ROLE_ADMIN_SEOUL_CENTRAL;
        PageRequest pageRequest = PageRequest.of(1, 3);

        //when
        Page<PendingClub> pendingClubs = pendingClubRepository.findAllByRequestTo(reqTo, pageRequest);

        //then
        Assertions.assertThat(pendingClubs.getTotalElements()).isEqualTo(pendingClubCnt / 3);
        Assertions.assertThat(pendingClubs.getTotalPages()).isEqualTo(3);
        Assertions.assertThat(pendingClubs.getNumber()).isEqualTo(1);
        Assertions.assertThat(pendingClubs.getNumberOfElements()).isEqualTo(3);
        for (PendingClub pendingClub : pendingClubs) {
            Assertions.assertThat(pendingClub.getRequestTo()).isEqualTo(Role.ROLE_ADMIN_SEOUL_CENTRAL);
        }
        Set<PendingClub> pendingClubSet = new HashSet<>(pendingClubs.stream().collect(Collectors.toList()));
        Assertions.assertThat(pendingClubSet).hasSize(pendingClubs.getSize());
    }

    @Test
    public void findAllByRequestTo_SearchByMaster_Success() throws Exception {
        //given
        Role reqTo = Role.ROLE_MASTER;
        PageRequest pageRequest = PageRequest.of(1, 3);

        //when
        Page<PendingClub> pendingClubs = pendingClubRepository.findAllByRequestTo(reqTo, pageRequest);

        //then
        Assertions.assertThat(pendingClubs.getTotalElements()).isEqualTo(pendingClubCnt / 3);
        Assertions.assertThat(pendingClubs.getTotalPages()).isEqualTo(3);
        Assertions.assertThat(pendingClubs.getNumber()).isEqualTo(1);
        Assertions.assertThat(pendingClubs.getNumberOfElements()).isEqualTo(3);
        for (PendingClub pendingClub : pendingClubs) {
            Assertions.assertThat(pendingClub.getRequestTo()).isEqualTo(Role.ROLE_MASTER);
        }
        Set<PendingClub> pendingClubSet = new HashSet<>(pendingClubs.stream().collect(Collectors.toList()));
        Assertions.assertThat(pendingClubSet).hasSize(pendingClubs.getSize());
    }

    @Test
    public void delete_GivenPendingClub_CannotFind() throws Exception {
        //given
        long pendingClubId = 1L;
        PendingClub pendingClub = pendingClubRepository.findById(pendingClubId).get();
        em.clear();

        //when
        pendingClubRepository.delete(pendingClub);
        em.flush();
        em.clear();

        //then
        List<PendingClub> pendingClubsAfterDeletion = pendingClubRepository.findAll();
        Assertions.assertThat(pendingClubsAfterDeletion).hasSize(pendingClubCnt - 1);
        em.clear();
        Assertions.assertThat(pendingClubRepository.findById(pendingClubId)).isEmpty();

    }
}
