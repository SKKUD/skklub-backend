package com.skklub.admin.repository;

import com.skklub.admin.domain.ExtraFile;
import com.skklub.admin.domain.Notice;
import com.skklub.admin.domain.Thumbnail;
import com.skklub.admin.domain.User;
import com.skklub.admin.domain.enums.Role;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.hibernate.proxy.HibernateProxy;
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

    @Test
    public void getThumbnail_FromFindedNotice_LazyCheck() throws Exception{
        //given
        String originalName = "testThumb.jpg";
        String uploadedName = "savedTestThumb.jpg";
        Thumbnail thumbnail = new Thumbnail(originalName, uploadedName);
        Notice notice = new Notice("testTitle", "testContent", null, thumbnail);
        em.persist(thumbnail);
        em.persist(notice);
        em.flush();
        em.clear();

        //when
        Optional<Notice> findedNotice = noticeRepository.findById(notice.getId());
        log.info("=======================Select For Lazy=============================");
        Thumbnail findedThumbnail = findedNotice.get().getThumbnail();

        //then
        Assertions.assertThat(findedThumbnail.getOriginalName()).isEqualTo(originalName);
        Assertions.assertThat(findedThumbnail.getUploadedName()).isEqualTo(uploadedName);
    }

    @Test
    public void noticeUpdate_FromSavedNotice_Success() throws Exception{
        //given
        Notice notice = new Notice("testTitle", "testContent", null, null);
        em.persist(notice);
        em.flush();
        em.clear();
        Notice updateInfo = new Notice("testTitle", "testContent2", null, null);

        //when
        Optional<Notice> findedNotice = noticeRepository.findById(notice.getId());
        findedNotice.get().update(updateInfo);
        em.flush();
        em.clear();

        //then
        Optional<Notice> afterChanged = noticeRepository.findById(notice.getId());
        Assertions.assertThat(afterChanged.get().getTitle()).isEqualTo("testTitle");
        Assertions.assertThat(afterChanged.get().getContent()).isEqualTo("testContent2");


    }

    @Test
    public void thumbnailUpdate_FromSavedNotice_Success() throws Exception{
        //given
        String originalName = "testThumb.jpg";
        String uploadedName = "savedTestThumb.jpg";
        Thumbnail thumbnail = new Thumbnail(originalName, uploadedName);
        Notice notice = new Notice("testTitle", "testContent", null, thumbnail);
        em.persist(thumbnail);
        em.persist(notice);
        em.flush();
        em.clear();
        Thumbnail changeInfo = new Thumbnail("testThumb2.jpg", "savedThumb2.jpg");

        //when
        Optional<Notice> findedNotice = noticeRepository.findById(notice.getId());
        Thumbnail findedThumbnail = findedNotice.get().getThumbnail();
        findedThumbnail.update(changeInfo);
        em.flush();
        em.clear();

        //then
        Optional<Notice> noticeAfterChanged = noticeRepository.findById(notice.getId());
        Thumbnail thumbnailAfterChanged = findedNotice.get().getThumbnail();
        Assertions.assertThat(thumbnailAfterChanged.getId()).isEqualTo(thumbnail.getId());
        Assertions.assertThat(thumbnailAfterChanged.getUploadedName()).isEqualTo(changeInfo.getUploadedName());
        Assertions.assertThat(thumbnailAfterChanged.getOriginalName()).isEqualTo(changeInfo.getOriginalName());
    }

    @Test
    public void noticeDelete_FromSavedNoticeWithFullFiles_CannotFind() throws Exception{
        //given
        Thumbnail thumbnail = new Thumbnail("testThumb.jpg", "savedTestThumb.jpg");
        Notice notice = new Notice("testTitle", "testContent", null, thumbnail);
        int fileCnt = 10;
        List<ExtraFile> extraFiles = new ArrayList<>();
        for (int i = 0; i < fileCnt; i++) {
            extraFiles.add(new ExtraFile("Test_Ex" + i + ".png", "saved_Test_Ex" + i + ".png"));
        }
        notice.appendExtraFiles(extraFiles);
        noticeRepository.save(notice);
        extraFileRepository.saveAll(extraFiles);
        em.flush();
        em.clear();

        //when
        Notice savedNotice = noticeRepository.findById(notice.getId()).get();
        noticeRepository.delete(savedNotice);
        em.flush();
        em.clear();

        //then
        Assertions.assertThat(noticeRepository.findById(notice.getId())).isEmpty();
        Assertions.assertThat(em.find(Thumbnail.class, thumbnail.getId())).isNull();
        extraFiles.stream()
                .map(ExtraFile::getId)
                .forEach(extraFilesId -> Assertions.assertThat(extraFileRepository.findById(extraFilesId).isEmpty()));
    }

    @Test
    public void findByOriginalNameAndNotice_FileNameInAnotherNotice_ReturnEmpty() throws Exception{
        //given
        Notice noticeA = new Notice("testTitle", "testContent", null, null);
        Notice noticeB = new Notice("testTitle", "testContent", null, null);
        int fileCnt = 10;
        List<ExtraFile> extraFiles = new ArrayList<>();
        for (int i = 0; i < fileCnt; i++) {
            extraFiles.add(new ExtraFile("Test_Ex" + i + ".png", "saved_Test_Ex" + i + ".png"));
        }
        noticeA.appendExtraFiles(extraFiles);
        noticeRepository.save(noticeA);
        noticeRepository.save(noticeB);
        extraFileRepository.saveAll(extraFiles);
        em.flush();
        em.clear();

        //when
        Optional<ExtraFile> findedExtraFileFromA = extraFileRepository.findByOriginalNameAndNotice("Test_Ex3.png", noticeA);
        Optional<ExtraFile> findedExtraFileFromB = extraFileRepository.findByOriginalNameAndNotice("Test_Ex3.png", noticeB);

        //then
        Assertions.assertThat(findedExtraFileFromA).isNotEmpty();
        Assertions.assertThat(findedExtraFileFromB).isEmpty();

    }

    @Test
    public void extraFileDelete_SavedNotice_CheckListSize() throws Exception{
        //given
        Notice notice = new Notice("testTitle", "testContent", null, null);
        int fileCnt = 10;
        List<ExtraFile> extraFiles = new ArrayList<>();
        for (int i = 0; i < fileCnt; i++) {
            extraFiles.add(new ExtraFile("Test_Ex" + i + ".png", "saved_Test_Ex" + i + ".png"));
        }
        notice.appendExtraFiles(extraFiles);
        noticeRepository.save(notice);
        extraFileRepository.saveAll(extraFiles);
        em.flush();
        em.clear();

        //when
        log.info("=============One Query===============");
        Optional<ExtraFile> findedExtraFile = extraFileRepository.findByOriginalNameAndNotice("Test_Ex3.png", notice);
        log.info("=============One Query===============");
        extraFileRepository.delete(findedExtraFile.get());
        em.flush();
        em.clear();

        //then
        Optional<Notice> afterDeletionNotice = noticeRepository.findById(notice.getId());
        Assertions.assertThat(afterDeletionNotice.get().getExtraFiles()).hasSize(fileCnt - 1);
        Assertions.assertThat(afterDeletionNotice.get().getExtraFiles()).containsExactly(
                extraFiles.get(0),
                extraFiles.get(1),
                extraFiles.get(2),
                extraFiles.get(4),
                extraFiles.get(5),
                extraFiles.get(6),
                extraFiles.get(7),
                extraFiles.get(8),
                extraFiles.get(9)
        );


    }
    
    @Test
    public void findDetailById_NoticeWithThumbnailAndUserAndExtraFiles_ThumbnailNotLoadedAndOneQuery() throws Exception{
        //given
        User user = new User("testUserId", "testUserPw", Role.ROLE_USER, "홍길동", "010-1111-1111");
        Thumbnail thumbnail = new Thumbnail("testThumb.jpg", "savedTestThumb.jpg");
        Notice notice = new Notice("testTitle", "testContent", user, thumbnail);
        int fileCnt = 10;
        List<ExtraFile> extraFiles = new ArrayList<>();
        for (int i = 0; i < fileCnt; i++) {
            extraFiles.add(new ExtraFile("Test_Ex" + i + ".png", "saved_Test_Ex" + i + ".png"));
        }
        notice.appendExtraFiles(extraFiles);
        userRepository.save(user);
        noticeRepository.save(notice);
        extraFileRepository.saveAll(extraFiles);
        em.flush();
        em.clear();
        
        //when
        Notice findedDetailNotice = noticeRepository.findDetailById(notice.getId()).get();

        //then
        log.info("=============================================");
        Assertions.assertThat(findedDetailNotice.getTitle()).isEqualTo(notice.getTitle());
        Assertions.assertThat(findedDetailNotice.getContent()).isEqualTo(notice.getContent());
        Assertions.assertThat(findedDetailNotice.getThumbnail()).isInstanceOf(HibernateProxy.class);
        Assertions.assertThat(findedDetailNotice.getWriter()).isNotInstanceOf(HibernateProxy.class);
        Assertions.assertThat(findedDetailNotice.getWriter().getName()).isEqualTo(user.getName());
        for (ExtraFile extraFile : findedDetailNotice.getExtraFiles()) {
            Assertions.assertThat(extraFile).isNotInstanceOf(HibernateProxy.class);
        }
        Assertions.assertThat(findedDetailNotice.getExtraFiles()).containsAll(extraFiles);
    }
    
    @Test
    public void findPreAndPost_10NoticeEverySecond_CheckIdOrder() throws Exception{
        //given
        int noticeCnt = 6;
        List<Notice> notices = new ArrayList<>();
        for(int i = 0; i < noticeCnt; i++){
            notices.add(new Notice("test title " + i, "test content " + i, null, null));
        }
        for (Notice notice : notices) {
            noticeRepository.save(notice);
            Thread.sleep(1000);
        }
        em.flush();
        em.clear();


        //when
        List<Notice> findedNotices = noticeRepository.findAllById(
                notices.stream().map(Notice::getId).collect(Collectors.toList())
        );
        em.flush();
        em.clear();
        int standIndex = 3;
        Optional<Notice> preNotice = noticeRepository.findPreByCreatedAt(findedNotices.get(standIndex).getCreatedAt());
        Optional<Notice> postNotice = noticeRepository.findPostByCreatedAt(findedNotices.get(standIndex).getCreatedAt());

        //then
        Assertions.assertThat(preNotice).isNotEmpty();
        Assertions.assertThat(postNotice).isNotEmpty();
        Assertions.assertThat(preNotice.get().getId()).isEqualTo(findedNotices.get(standIndex - 1).getId());
        Assertions.assertThat(postNotice.get().getId()).isEqualTo(findedNotices.get(standIndex + 1).getId());
    }


    @Test
    public void findPreAndPost_NoPre_CheckIdOrder() throws Exception{
        //given
        int noticeCnt = 6;
        List<Notice> notices = new ArrayList<>();
        for(int i = 0; i < noticeCnt; i++){
            notices.add(new Notice("test title " + i, "test content " + i, null, null));
        }
        for (Notice notice : notices) {
            noticeRepository.save(notice);
            Thread.sleep(1000);
        }
        em.flush();
        em.clear();


        //when
        List<Notice> findedNotices = noticeRepository.findAllById(
                notices.stream().map(Notice::getId).collect(Collectors.toList())
        );
        em.flush();
        em.clear();
        int standIndex = 0;
        Optional<Notice> preNotice = noticeRepository.findPreByCreatedAt(findedNotices.get(standIndex).getCreatedAt());
        Optional<Notice> postNotice = noticeRepository.findPostByCreatedAt(findedNotices.get(standIndex).getCreatedAt());

        //then
        Assertions.assertThat(preNotice).isEmpty();
        Assertions.assertThat(postNotice).isNotEmpty();
        Assertions.assertThat(postNotice.get().getId()).isEqualTo(findedNotices.get(standIndex + 1).getId());
    }

    @Test
    public void findPreAndPost_NoPre_CheckIdOrder() throws Exception{
        //given
        int noticeCnt = 6;
        List<Notice> notices = new ArrayList<>();
        for(int i = 0; i < noticeCnt; i++){
            notices.add(new Notice("test title " + i, "test content " + i, null, null));
        }
        for (Notice notice : notices) {
            noticeRepository.save(notice);
            Thread.sleep(1000);
        }
        em.flush();
        em.clear();


        //when
        List<Notice> findedNotices = noticeRepository.findAllById(
                notices.stream().map(Notice::getId).collect(Collectors.toList())
        );
        em.flush();
        em.clear();
        int standIndex = noticeCnt - 1;
        Optional<Notice> preNotice = noticeRepository.findPreByCreatedAt(findedNotices.get(standIndex).getCreatedAt());
        Optional<Notice> postNotice = noticeRepository.findPostByCreatedAt(findedNotices.get(standIndex).getCreatedAt());

        //then
        Assertions.assertThat(preNotice).isNotEmpty();
        Assertions.assertThat(preNotice.get().getId()).isEqualTo(findedNotices.get(standIndex - 1).getId());
        Assertions.assertThat(postNotice).isEmpty();
    }
}
