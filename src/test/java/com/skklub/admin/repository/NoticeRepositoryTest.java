package com.skklub.admin.repository;

import com.skklub.admin.domain.ExtraFile;
import com.skklub.admin.domain.Notice;
import com.skklub.admin.domain.Thumbnail;
import com.skklub.admin.domain.User;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class NoticeRepositoryTest {
    @Autowired
    private EntityManager em;
    @Autowired
    private NoticeRepository noticeRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ExtraFileRepository extraFileRepository;

    @Test
    public void noticeSave_WithThumbnailAndSavedUser_RelationCheck() throws Exception{
        //given
         String title = "Test Title";
        String content = "Test Content";
        User user = userRepository.findByUsername("userId0");
        Thumbnail thumbnail = new Thumbnail("Test_Thumb.png", "saved_Test_Thumb.png");
        Notice notice = new Notice(title, content, user, thumbnail);

        //when
        noticeRepository.save(notice);
        em.flush();
        em.clear();

        //then
        Long noticeId = notice.getId();
        Optional<Notice> findedNotice = noticeRepository.findById(noticeId);
        Assertions.assertThat(findedNotice).isNotEmpty();
        Assertions.assertThat(findedNotice.get().getTitle()).isEqualTo(title);
        Assertions.assertThat(findedNotice.get().getContent()).isEqualTo(content);
        Assertions.assertThat(findedNotice.get().getThumbnail()).isEqualTo(thumbnail);
        Assertions.assertThat(findedNotice.get().getWriter()).isEqualTo(user);
    }

    @Test
    public void extraFileSave_WhenEmptyList_SaveOrderCheck() throws Exception{
        //given
        String title = "Test Title";
        String content = "Test Content";
        User user = userRepository.findByUsername("userId0");
        Thumbnail thumbnail = new Thumbnail("Test_Thumb.png", "saved_Test_Thumb.png");
        Notice notice = new Notice(title, content, user, thumbnail);
        noticeRepository.save(notice);
        em.flush();
        em.clear();
        Long noticeId = notice.getId();
        int fileCnt = 10;
        List<ExtraFile> extraFiles = new ArrayList<>();
        for (int i = 0; i < fileCnt; i++) {
            extraFiles.add(new ExtraFile("Test_Ex" + i + ".png", "saved_Test_Ex" + i + ".png"));
        }

        //when
        Notice findedNotice = noticeRepository.findById(noticeId).get();
        findedNotice.appendExtraFiles(extraFiles);
        extraFileRepository.saveAll(extraFiles);
        em.flush();
        em.clear();

        //then
        List<ExtraFile> findedExtraFiles = extraFileRepository.findAllById(extraFiles.stream().map(ExtraFile::getId).collect(Collectors.toList()));
        Assertions.assertThat(findedExtraFiles.size()).isEqualTo(extraFiles.size());
        Assertions.assertThat(findedExtraFiles).containsAll(extraFiles);
        for (ExtraFile findedExtraFile : findedExtraFiles) {
            Assertions.assertThat(findedExtraFile.getNotice().getId()).isEqualTo(noticeId);
        }
        em.flush();
        em.clear();
        List<ExtraFile> extraFilesFromNotice = noticeRepository.findById(noticeId).get().getExtraFiles();
        Assertions.assertThat(extraFilesFromNotice.size()).isEqualTo(extraFiles.size());
        Assertions.assertThat(extraFilesFromNotice).containsAll(extraFiles);
    }

    @Test
    public void extraFileSave_AddToList_SaveOrderCheck() throws Exception{
        //given
        String title = "Test Title";
        String content = "Test Content";
        User user = userRepository.findByUsername("userId0");
        Thumbnail thumbnail = new Thumbnail("Test_Thumb.png", "saved_Test_Thumb.png");
        Notice notice = new Notice(title, content, user, thumbnail);
        int beforeFileCnt = 10;
        List<ExtraFile> beforeExtraFiles = new ArrayList<>();
        for (int i = 0; i < beforeFileCnt; i++) {
            beforeExtraFiles.add(new ExtraFile("Test_Ex" + i + ".png", "saved_Test_Ex" + i + ".png"));
        }
        notice.appendExtraFiles(beforeExtraFiles);
        noticeRepository.save(notice);
        extraFileRepository.saveAll(beforeExtraFiles);
        em.flush();
        em.clear();
        Long noticeId = notice.getId();
        int fileCnt = 10;
        List<ExtraFile> extraFiles = new ArrayList<>();
        for (int i = beforeFileCnt; i < beforeFileCnt + fileCnt; i++) {
            extraFiles.add(new ExtraFile("Test_Ex" + i + ".png", "saved_Test_Ex" + i + ".png"));
        }

        //when
        Notice findedNotice = noticeRepository.findById(noticeId).get();
        findedNotice.appendExtraFiles(extraFiles);
        extraFileRepository.saveAll(extraFiles);
        em.flush();
        em.clear();

        //then
        List<ExtraFile> findedExtraFiles = extraFileRepository.findAllById(extraFiles.stream().map(ExtraFile::getId).collect(Collectors.toList()));
        Assertions.assertThat(findedExtraFiles.size()).isEqualTo(beforeFileCnt);
        Assertions.assertThat(findedExtraFiles).containsAll(extraFiles);
        for (ExtraFile findedExtraFile : findedExtraFiles) {
            Assertions.assertThat(findedExtraFile.getNotice().getId()).isEqualTo(noticeId);
        }
        em.flush();
        em.clear();
        List<ExtraFile> extraFilesFromNotice = noticeRepository.findById(noticeId).get().getExtraFiles();
        Assertions.assertThat(extraFilesFromNotice.size()).isEqualTo(fileCnt + beforeFileCnt);
        Assertions.assertThat(extraFilesFromNotice).containsAll(extraFiles);
    }
}
