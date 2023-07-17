package com.skklub.admin.integration.notice;

import com.skklub.admin.controller.NoticeController;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
public class NoticeCommandIntegrationTest {
    @Autowired
    private NoticeController noticeController;

    @Test
    public void createNotice_WithThumbnail_Success() throws Exception{
        //given

        //when

        //then

    }

    @Test
    public void createNotice_WithoutThumbnail_Success() throws Exception{
        //given

        //when

        //then

    }

    @Test
    public void appendFile_ToEmptyFiles_Success() throws Exception{
        //given

        //when

        //then

    }

    @Test
    public void appendFile_ToFilledFiles_CheckCnt() throws Exception{
        //given

        //when

        //then

    }

    @Test
    public void appendFile_BadNoticeId_NoticeIdMisMatchException() throws Exception{
        //given

        //when

        //then

    }
    
    
    @Test
    public void updateNotice_Default_CheckChangedInfo() throws Exception{
        //given
        
        //when
        
        //then
        
    }

    @Test
    public void updateNotice_BadNoticeId_NoticeIdMistMatchException() throws Exception {
        //given

        //when

        //then

    }
    
    @Test
    public void updateThumbnail_FromDefaultThumbnail_NoS3Deletion() throws Exception{
        //given
        
        //when
        
        //then
        
    }

    @Test
    public void updateThumbnail_FromSomeThumbnail_WithS3Deletion() throws Exception{
        //given

        //when

        //then

    }

    @Test
    public void updateThumbnail_BadNoticeId_NoticeIdMisMatchException() throws Exception{
        //given

        //when

        //then

    }


    @Test
    public void deleteNotice_DefaultThumbnail_NoS3Deletion() throws Exception{
        //given

        //when

        //then

    }

    @Test
    public void deleteNotice_WithThumbnail_S3Deletion() throws Exception{
        //given

        //when

        //then

    }

    @Test
    public void deleteNotice_NoExtraFile_SkipS3Deletion() throws Exception{
        //given

        //when

        //then

    }

    @Test
    public void deleteNotice_WithExtraFiles_S3BulkDeletion() throws Exception{
        //given

        //when

        //then

    }

    @Test
    public void deleteNotice_BadNoticeId_NoticeIdMistMatchException() throws Exception{
        //given

        //when

        //then

    }

    @Test
    public void deleteFileByOriginalName_Default_CannotFindFromNotice() throws Exception{
        //given
        
        //when
        
        //then
        
    }

    @Test
    public void deleteFileByOriginalName_CannotFindFileInNotice_ExtraFileNameMisMatchException() throws Exception{
        //given

        //when

        //then

    }

    @Test
    public void deleteFileByOriginalName_BadNoticeID_NoticeIdMisMatchException() throws Exception{
        //given

        //when

        //then

    }

}
