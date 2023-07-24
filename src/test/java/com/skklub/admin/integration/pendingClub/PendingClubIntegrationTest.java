package com.skklub.admin.integration.pendingClub;

import com.skklub.admin.controller.PendingClubController;
import com.skklub.admin.repository.ClubRepository;
import com.skklub.admin.repository.PendingClubRepository;
import com.skklub.admin.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional
@SpringBootTest
public class PendingClubIntegrationTest {
    @Autowired
    private PendingClubController pendingClubController;
    @Autowired
    private PendingClubRepository pendingClubRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ClubRepository clubRepository;

    @Test
    public void createPending_ToAdminSeoul_SaveWell() throws Exception{
        //given

        //when

        //then

    }

    @Test
    public void createPending_ToMaster_SaveWell() throws Exception{
        //given

        //when

        //then

    }

    @Test
    public void getPendingList_LoginWithSuwonCentralAdmin_OnlyIncludeSuwonAdmin() throws Exception{
        //given
        
        //when
        
        //then
        
    }

    @Test
    public void getPendingList_LoginWithMaster_OnlyIncludeMaster() throws Exception{
        //given

        //when

        //then

    }

    @Test
    public void acceptPending_GivenPendingClub_SaveClubWithDefaultLogoAndUserWellAndDeletePendingClub() throws Exception{
        //given

        //when

        //then

    }

    @Test
    public void denyPending_GivenPendingClub_DeletePendingClub() throws Exception{
        //given

        //when

        //then

    }

}
