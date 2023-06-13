package com.skklub.admin.service;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


@Slf4j
@SpringBootTest
class ClubServiceTest {

    @Autowired
    private ClubService clubService;


    @Test
    public void deleteReviveClub_noMatchId_returnEmptyOptional() throws Exception{
        //given
        Long id = -1L;
        //when
        Optional<String> d = clubService.deleteClub(id);
        Optional<String> r = clubService.reviveClub(id);

        //then
        Assertions.assertThat(r).isEmpty();
        Assertions.assertThat(d).isEmpty();

     }

}