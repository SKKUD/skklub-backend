package com.skklub.admin.service;

import com.skklub.admin.domain.ExtraFile;
import com.skklub.admin.domain.Notice;
import com.skklub.admin.domain.Thumbnail;
import com.skklub.admin.domain.User;
import com.skklub.admin.repository.ExtraFileRepository;
import com.skklub.admin.repository.NoticeRepository;
import com.skklub.admin.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class NoticeServiceTest {
    @InjectMocks
    private NoticeService noticeService;
    @Mock
    private NoticeRepository noticeRepository;
    @Mock
    private ExtraFileRepository extraFileRepository;
    @Mock
    private UserRepository userRepository;

    @Test
    public void appendExtraFiles_AddToEmptyList_BiRelation() throws Exception{
        //given
        String title = "Test Title";
        String content = "Test Content";
        Notice notice = new Notice(title, content, null, null);
        int fileCnt = 10;
        List<ExtraFile> extraFiles = new ArrayList<>();
        for (int i = 0; i < fileCnt; i++) {
            extraFiles.add(new ExtraFile("Test_Ex" + i + ".png", "saved_Test_Ex" + i + ".png"));
        }
        doAnswer(
            invocation
                -> null
        ).when(extraFileRepository).saveAll(extraFiles);

        //when
        noticeService.appendExtraFiles(notice, extraFiles);

        //then
        Assertions.assertThat(notice.getExtraFiles()).hasSize(fileCnt);
        Assertions.assertThat(notice.getExtraFiles()).containsAll(extraFiles);
    }

    @Test
    public void appendExtraFiles_AddToList_BiRelation() throws Exception{
        //given
        String title = "Test Title";
        String content = "Test Content";
        Notice notice = new Notice(title, content, null, null);
        int fileCnt = 10;
        List<ExtraFile> extraFiles = new ArrayList<>();
        for (int i = 0; i < fileCnt; i++) {
            notice.getExtraFiles().add(new ExtraFile("Test_Ex" + i + ".png", "saved_Test_Ex" + i + ".png"));
            extraFiles.add(new ExtraFile("Test_Ex" + i + ".png", "saved_Test_Ex" + i + ".png"));
        }
        doAnswer(
                invocation
                        -> null
        ).when(extraFileRepository).saveAll(extraFiles);

        //when
        noticeService.appendExtraFiles(notice, extraFiles);

        //then
        Assertions.assertThat(notice.getExtraFiles()).hasSize(fileCnt * 2);
        Assertions.assertThat(notice.getExtraFiles()).containsAll(extraFiles);
    }


}
