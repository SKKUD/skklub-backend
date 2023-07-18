package com.skklub.admin.integration.notice;

import com.skklub.admin.controller.NoticeController;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
public class NoticeQueryIntegrationTest {
    @Autowired
    private NoticeController noticeController;
    
    @Test
    public void getDetailNotice_WithPreAndPost_ReturnBoth() throws Exception{
        //given
        
        //when
        
        //then
        
    }

    @Test
    public void getDetailNotice_NoPre_ReturnNullInPre() throws Exception{
        //given

        //when

        //then

    }

    @Test
    public void getDetailNotice_NoPreAndPost_ReturnNullInBoth() throws Exception{
        //given

        //when

        //then

    }
    
    @Test
    public void getFile_Default_CheckHeaderAndBody() throws Exception{
        //given
        
        //when
        
        //then
        
    }
    
    @Test
    public void getNoticePrevWithThumbnail_Default_FindDefaultOrSomeThumbnail() throws Exception{
        //given
        
        //when
        
        //then
        
    }

    @Test
    public void getNoticePrev_findAll_Success() throws Exception{
        //given

        //when

        //then

    }

    @Test
    public void getNoticePrev_SearchBySomeAdminRole_Success() throws Exception{
        //given

        //when

        //then

    }

    @Test
    public void getNoticePrev_SearchByUserRole_CannotCategorizeByUserException() throws Exception{
        //given

        //when

        //then

    }

    @Test
    public void getNoticePrev_SearchByMasterRole_CannotCategorizeByMasterException() throws Exception{
        //given

        //when

        //then

    }
    
    @Test
    public void getNoticePrevByTitle_FirstMiddleLastMatch_FindAll() throws Exception{
        //given
        
        //when
        
        //then
        
    }
}
