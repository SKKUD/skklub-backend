package com.skklub.admin.repository;

import com.skklub.admin.TestDataRepository;
import com.skklub.admin.domain.Club;
import com.skklub.admin.domain.Logo;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestDataRepository.class)
class LogoRepositoryTest {
    @Autowired
    private EntityManager em;
    @Autowired
    private ClubRepository clubRepository;
    @Autowired
    private LogoRepository logoRepository;
    @Autowired
    private TestDataRepository testDataRepository;

    @BeforeEach
    public void beforeEach() {
        testDataRepository = new TestDataRepository();
    }

    @Test
    public void findByClubId_Default_OnlyOneQueryForLogo() throws Exception{
        //given
        Club club = testDataRepository.getCleanClub(0);
        Logo logo = testDataRepository.getLogos().get(0);
        club.changeLogo(logo);
        // 2 Insert
        logoRepository.save(logo);
        clubRepository.save(club);
        em.flush();
        em.clear();

        //when
        //1 Select
        Optional<Logo> findedLogo = logoRepository.findByClubId(club.getId());
        em.flush();
        em.clear();

        //then
        //1 Select
        Optional<Club> findedClub = clubRepository.findById(club.getId());
        findedClub.ifPresent(
                //1 Select
                c -> Assertions.assertThat(c.getLogo()).isEqualTo(findedLogo.get())
        );


    }
}