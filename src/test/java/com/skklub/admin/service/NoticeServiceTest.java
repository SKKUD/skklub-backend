package com.skklub.admin.service;

import com.skklub.admin.domain.ExtraFile;
import com.skklub.admin.domain.Notice;
import com.skklub.admin.domain.Thumbnail;
import com.skklub.admin.exception.deprecated.error.exception.NoticeIdMisMatchException;
import com.skklub.admin.repository.ExtraFileRepository;
import com.skklub.admin.repository.NoticeRepository;
import com.skklub.admin.repository.UserRepository;
import com.skklub.admin.service.dto.FileNames;
import com.skklub.admin.service.dto.NoticeDeletionDto;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
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
    public void createNotice_WithThumbAndFiles_CheckExtraFilesRelations() throws Exception{
        //given
        String title = "Test Title";
        String content = "Test Content";
        Notice notice = new Notice(title, content, null, null);
        List<ExtraFile> extraFiles = new ArrayList<>();
         int fileCnt = 10;
        for (int i = 0; i < fileCnt; i++) {
            extraFiles.add(new ExtraFile("Test_Ex" + i + ".png", "saved_Test_Ex" + i + ".png"));
        }

        //when
        notice.appendExtraFiles(extraFiles);

        //then
        for (ExtraFile extraFile : extraFiles) {
            Assertions.assertThat(extraFile.getNotice().getTitle()).isEqualTo(title);
            Assertions.assertThat(extraFile.getNotice().getContent()).isEqualTo(content);
        }
        Assertions.assertThat(notice.getExtraFiles()).hasSize(fileCnt);
    }

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

    @Test
    public void updateNotice_GivenNotice_ChangeInfo() throws Exception{
        //given
        long noticeId = 0L;
        Notice notice = new Notice("testTitle", "testContent", null, null);
        given(noticeRepository.findById(noticeId)).willReturn(Optional.of(notice));
        Notice changeInfo = new Notice("test Title2", "test Content2", null, null);

        //when
        Optional<String> changedTitle = noticeService.updateNotice(noticeId, changeInfo);

        //then
        Assertions.assertThat(changedTitle.get()).isEqualTo(changeInfo.getTitle());
        Assertions.assertThat(notice.getTitle()).isEqualTo(changeInfo.getTitle());
        Assertions.assertThat(notice.getContent()).isEqualTo(changeInfo.getContent());
    }

    @Test
    public void updateThumbnail_GivenNoticeAndThumbnail_Success() throws Exception{
        //given
        long noticeId = 0L;
        String originalName = "testThumb.jpg";
        String uploadedName = "savedTestThumb.jpg";
        Thumbnail thumbnail = new Thumbnail(originalName, uploadedName);
        Notice notice = new Notice("testTitle", "testContent", null, thumbnail);
        given(noticeRepository.findById(noticeId)).willReturn(Optional.of(notice));
        Thumbnail changeInfo = new Thumbnail("testThumb2.jpg", "savedTestThumb2.jpg");

        //when
        FileNames oldThumbnail = noticeService.updateThumbnail(noticeId, changeInfo).get();

        //then
        Assertions.assertThat(oldThumbnail.getOriginalName()).isEqualTo(originalName);
        Assertions.assertThat(oldThumbnail.getSavedName()).isEqualTo(uploadedName);
        Assertions.assertThat(thumbnail.getOriginalName()).isEqualTo(changeInfo.getOriginalName());
        Assertions.assertThat(thumbnail.getUploadedName()).isEqualTo(changeInfo.getUploadedName());
        Assertions.assertThat(notice.getThumbnail().getOriginalName()).isEqualTo(changeInfo.getOriginalName());
        Assertions.assertThat(notice.getThumbnail().getUploadedName()).isEqualTo(changeInfo.getUploadedName());
    }
    
    @Test
    public void deleteNotice_GivenNoticeWithFullFiles_CheckReturnDTO() throws Exception{
        //given
        long noticeId = 0L;
        Thumbnail thumbnail = new Thumbnail("testThumb.jpg", "savedTestThumb.jpg");
        Notice notice = new Notice("testTitle", "testContent", null, thumbnail);
        int fileCnt = 10;
        List<ExtraFile> extraFiles = new ArrayList<>();
        for (int i = 0; i < fileCnt; i++) {
            extraFiles.add(new ExtraFile("Test_Ex" + i + ".png", "saved_Test_Ex" + i + ".png"));
        }
        notice.appendExtraFiles(extraFiles);
        given(noticeRepository.findById(noticeId)).willReturn(Optional.of(notice));
        doNothing().when(noticeRepository).delete(notice);
        
        //when
        NoticeDeletionDto noticeDeletionDto = noticeService.deleteNotice(noticeId).get();

        //then
        Assertions.assertThat(noticeDeletionDto.getNoticeTitle()).isEqualTo(notice.getTitle());
        Assertions.assertThat(noticeDeletionDto.getThumbnailFileName().getSavedName()).isEqualTo(thumbnail.getUploadedName());
        Assertions.assertThat(noticeDeletionDto.getThumbnailFileName().getOriginalName()).isEqualTo(thumbnail.getOriginalName());
        Assertions.assertThat(noticeDeletionDto.getExtraFileNames()).hasSize(fileCnt);
        Assertions.assertThat(noticeDeletionDto.getExtraFileNames()).containsAll(
                extraFiles.stream().map(FileNames::new).collect(Collectors.toList())
        );
    }

    @Test
    public void deleteExtraFile() throws Exception{
        //given
        Long noticeId = 0L;
        Notice notice = new Notice("testTitle", "testContent", null, null);
        int fileCnt = 10;
        List<ExtraFile> extraFiles = new ArrayList<>();
        for (int i = 0; i < fileCnt; i++) {
            extraFiles.add(new ExtraFile("Test_Ex" + i + ".png", "saved_Test_Ex" + i + ".png"));
        }
        notice.appendExtraFiles(extraFiles);
        given(noticeRepository.findById(noticeId)).willReturn(Optional.of(notice));
        given(extraFileRepository.findByOriginalNameAndNotice("Test_Ex3.png", notice)).willReturn(Optional.ofNullable(extraFiles.get(3)));
        doNothing().when(extraFileRepository).delete(extraFiles.get(3));
        
        //when
        FileNames deletedFileName = noticeService.deleteExtraFile(noticeId, "Test_Ex3.png").get();

        //then
        Assertions.assertThat(deletedFileName.getOriginalName()).isEqualTo("Test_Ex3.png");
        Assertions.assertThat(deletedFileName.getSavedName()).isEqualTo("saved_Test_Ex3.png");
    }

    @Test
    public void deleteExtraFile_BadNoticeId_NoticeIdMisMatchException() throws Exception{
        //given
        Long noticeId = -1L;
        given(noticeRepository.findById(noticeId)).willReturn(Optional.empty());

        //when
        assertThrows(NoticeIdMisMatchException.class, () -> noticeService.deleteExtraFile(noticeId, "anyString"));

        //then

    }

}
