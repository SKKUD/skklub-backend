package com.skklub.admin.integration;

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

}
