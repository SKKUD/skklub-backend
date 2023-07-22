package com.skklub.admin.controller.pendingClub;

import com.skklub.admin.controller.PendingClubController;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@WithMockUser
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@WebMvcTest(controllers = PendingClubControllerTest.class)
@MockBean(JpaMetamodelMappingContext.class)
class PendingClubControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private PendingClubController pendingClubController;

    @Test
    public void createPending_Default_Success() throws Exception{
        //given

        //when

        //then

    }

    @Test
    public void createPending_ReqToUser_CannotRequestCreationToUserException() throws Exception{
        //given

        //when

        //then

    }

    @Test
    public void createPending_GivenSomeNull_BindException() throws Exception{
        //given

        //when

        //then

    }

    @Test
    public void getPendingList_Default_Success() throws Exception{
        //given

        //when

        //then

    }

    @Test
    public void getPendingList_LoginWithUser_InvalidApproachException() throws Exception{
        //given

        //when

        //then

    }
    
    @Test
    public void acceptPending_Default_Success() throws Exception{
        //given
        
        //when
        
        //then
        
    }
    
    @Test
    public void acceptPending_BadBelongs_InvalidBelongsException() throws Exception{
        //given
        
        //when
        
        //then
        
    }
    
    @Test
    public void acceptPending_BadPendingClubId_PendingClubIdMisMatchException() throws Exception{
        //given
        
        //when
        
        //then
        
    }

    @Test
    public void denyPending_Default_Success() throws Exception{
        //given

        //when

        //then

    }

    @Test
    public void denyPending_BadPendingClubId_PendingClubIdMisMatchException() throws Exception{
        //given

        //when

        //then

    }
}