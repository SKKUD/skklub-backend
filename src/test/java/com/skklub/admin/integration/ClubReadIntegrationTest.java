package com.skklub.admin.integration;

import com.skklub.admin.controller.ClubController;
import com.skklub.admin.repository.ClubRepository;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional
@SpringBootTest
public class ClubReadIntegrationTest {
    @Autowired
    private EntityManager em;
    @Autowired
    private ClubController clubController;
    @Autowired
    private ClubRepository clubRepository;

    @Test
    public void getClubById_() throws Exception{
        //given


        //when

        //then

    }
}
