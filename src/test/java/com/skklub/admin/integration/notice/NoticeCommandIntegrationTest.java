package com.skklub.admin.integration.notice;

import com.amazonaws.services.s3.AmazonS3;
import com.skklub.admin.WithMockCustomUser;
import com.skklub.admin.controller.NoticeController;
import com.skklub.admin.controller.dto.*;
import com.skklub.admin.domain.ExtraFile;
import com.skklub.admin.domain.Notice;
import com.skklub.admin.domain.Thumbnail;
import com.skklub.admin.domain.User;
import com.skklub.admin.domain.enums.Role;
import com.skklub.admin.exception.deprecated.error.exception.ExtraFileNameMisMatchException;
import com.skklub.admin.exception.deprecated.error.exception.NoticeIdMisMatchException;
import com.skklub.admin.repository.ExtraFileRepository;
import com.skklub.admin.repository.NoticeRepository;
import com.skklub.admin.security.auth.PrincipalDetailsService;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.annotation.Commit;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
@SpringBootTest
@Transactional
@WithMockCustomUser(username = "testMasterID",role = Role.ROLE_MASTER)
public class NoticeCommandIntegrationTest {
    @Autowired
    private NoticeController noticeController;
    @Autowired
    private AmazonS3 amazonS3;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    @Autowired
    private NoticeRepository noticeRepository;
    @Autowired
    private ExtraFileRepository extraFileRepository;
    @Autowired
    private EntityManager em;
    @Autowired
    private PrincipalDetailsService principalDetailsService;

    @Test
    @WithMockCustomUser(username = "testAdminID0")
    public void createNotice_WithThumbnailAndFiles_Success() throws Exception{
        //given
        UserDetails userDetails = principalDetailsService.loadUserByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        MultipartFile thumbnail = readyThumbnail();
        int fileCnt = 10;
        List<MultipartFile> files = readyFiles(fileCnt);
        NoticeCreateRequest noticeCreateRequest = new NoticeCreateRequest("creation test title", "creation test content");

        //when
        NoticeIdAndTitleResponse response = noticeController.createNotice(noticeCreateRequest, thumbnail, Optional.of(files), userDetails);
        em.flush();
        em.clear();

        //then
        Optional<Notice> savedNotice = noticeRepository.findDetailById(response.getId());
        Assertions.assertThat(savedNotice).isNotEmpty();
        Assertions.assertThat(savedNotice.get().getTitle()).isEqualTo(noticeCreateRequest.getTitle());
        Assertions.assertThat(savedNotice.get().getContent()).isEqualTo(noticeCreateRequest.getContent());
        Assertions.assertThat(savedNotice.get().getThumbnail().getOriginalName()).isEqualTo(thumbnail.getOriginalFilename());
        Assertions.assertThat(amazonS3.doesObjectExist(bucket, savedNotice.get().getThumbnail().getUploadedName())).isTrue();
        Assertions.assertThat(savedNotice.get().getWriter().getName()).isEqualTo("testAdminName0");
        Assertions.assertThat(savedNotice.get().getWriter().getUsername()).isEqualTo("testAdminID0");
        List<ExtraFile> extraFiles = savedNotice.get().getExtraFiles();
        Assertions.assertThat(extraFiles).hasSize(fileCnt);
        for (ExtraFile extraFile : extraFiles) {
            Assertions.assertThat(extraFile.getNotice().getId()).isEqualTo(savedNotice.get().getId());
            Assertions.assertThat(amazonS3.doesObjectExist(bucket, extraFile.getSavedName())).isTrue();
            Assertions.assertThat(extraFile.getOriginalName()).isEqualTo("9.pdf");
        }
    }

    @Test
    @WithMockCustomUser(username = "testAdminID0")
    public void createNotice_WithoutThumbnailWithFiles_Success() throws Exception{
        //given
        int fileCnt = 10;
        List<MultipartFile> files = readyFiles(fileCnt);
        UserDetails userDetails = principalDetailsService.loadUserByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        NoticeCreateRequest noticeCreateRequest = new NoticeCreateRequest("creation test title", "creation test content");

        //when
        NoticeIdAndTitleResponse response = noticeController.createNotice(noticeCreateRequest, null, Optional.of(files), userDetails);
        em.flush();
        em.clear();

        //then
        Optional<Notice> savedNotice = noticeRepository.findDetailById(response.getId());
        Assertions.assertThat(savedNotice).isNotEmpty();
        Assertions.assertThat(savedNotice.get().getTitle()).isEqualTo(noticeCreateRequest.getTitle());
        Assertions.assertThat(savedNotice.get().getContent()).isEqualTo(noticeCreateRequest.getContent());
        Assertions.assertThat(savedNotice.get().getThumbnail().getOriginalName()).isEqualTo("default_thumb.png");
        Assertions.assertThat(savedNotice.get().getThumbnail().getUploadedName()).isEqualTo("default_thumb.png");
        Assertions.assertThat(amazonS3.doesObjectExist(bucket, savedNotice.get().getThumbnail().getUploadedName())).isTrue();
        Assertions.assertThat(savedNotice.get().getWriter().getName()).isEqualTo("testAdminName0");
        Assertions.assertThat(savedNotice.get().getWriter().getUsername()).isEqualTo("testAdminID0");
        List<ExtraFile> extraFiles = savedNotice.get().getExtraFiles();
        Assertions.assertThat(extraFiles).hasSize(fileCnt);
        for (ExtraFile extraFile : extraFiles) {
            Assertions.assertThat(extraFile.getNotice().getId()).isEqualTo(savedNotice.get().getId());
            Assertions.assertThat(amazonS3.doesObjectExist(bucket, extraFile.getSavedName())).isTrue();
            Assertions.assertThat(extraFile.getOriginalName()).isEqualTo("9.pdf");
        }

    }

    @Test
    @WithMockCustomUser(username = "testAdminID0")
    public void createNotice_WithThumbnailWithoutFiles_Success() throws Exception{
        //given
        UserDetails userDetails = principalDetailsService.loadUserByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        NoticeCreateRequest noticeCreateRequest = new NoticeCreateRequest("creation test title", "creation test content");

        //when
        NoticeIdAndTitleResponse response = noticeController.createNotice(noticeCreateRequest, null, Optional.empty(), userDetails);
        em.flush();
        em.clear();

        //then
        Optional<Notice> savedNotice = noticeRepository.findDetailById(response.getId());
        Assertions.assertThat(savedNotice).isNotEmpty();
        Assertions.assertThat(savedNotice.get().getTitle()).isEqualTo(noticeCreateRequest.getTitle());
        Assertions.assertThat(savedNotice.get().getContent()).isEqualTo(noticeCreateRequest.getContent());
        Assertions.assertThat(savedNotice.get().getThumbnail().getOriginalName()).isEqualTo("default_thumb.png");
        Assertions.assertThat(savedNotice.get().getThumbnail().getUploadedName()).isEqualTo("default_thumb.png");
        Assertions.assertThat(amazonS3.doesObjectExist(bucket, savedNotice.get().getThumbnail().getUploadedName())).isTrue();
        Assertions.assertThat(savedNotice.get().getWriter().getName()).isEqualTo("testAdminName0");
        Assertions.assertThat(savedNotice.get().getWriter().getUsername()).isEqualTo("testAdminID0");

    }

    @Test
    public void appendFile_ToEmptyFiles_Success() throws Exception{
        //given
        Long noticeId = 1L;
        Optional<Notice> notice = noticeRepository.findDetailById(noticeId);
        Assertions.assertThat(notice).isNotEmpty();
        Assertions.assertThat(notice.get().getExtraFiles()).isEmpty();
        em.clear();
        int fileCnt = 5;
        List<MultipartFile> extraFiles = readyFiles(fileCnt);

        //when
        NoticeIdAndFileCountResponse response = noticeController.appendFile(noticeId, extraFiles);
        em.flush();
        em.clear();

        //then
        Assertions.assertThat(response.getNoticeId()).isEqualTo(noticeId);
        Assertions.assertThat(response.getFileCnt()).isEqualTo(fileCnt);
        Notice noticeAfterFileAppend = noticeRepository.findDetailById(noticeId).get();
        List<ExtraFile> appendedExtraFiles = noticeAfterFileAppend.getExtraFiles();
        Assertions.assertThat(appendedExtraFiles).hasSize(fileCnt);
        for (ExtraFile appendedExtraFile : appendedExtraFiles) {
            Assertions.assertThat(appendedExtraFile.getOriginalName()).isEqualTo("9.pdf");
            Assertions.assertThat(amazonS3.doesObjectExist(bucket, appendedExtraFile.getSavedName())).isTrue();
            Assertions.assertThat(appendedExtraFile.getNotice().getId()).isEqualTo(noticeId);
        }
    }

    @Test
    public void appendFile_ToFilledFiles_CheckCnt() throws Exception{
        //given
        Long noticeId = 5L;
        Optional<Notice> notice = noticeRepository.findDetailById(noticeId);
        Assertions.assertThat(notice).isNotEmpty();
        Assertions.assertThat(notice.get().getExtraFiles()).isNotEmpty();
        int existCnt = notice.get().getExtraFiles().size();
        em.clear();
        int fileCnt = 5;
        List<MultipartFile> extraFiles = readyFiles(fileCnt);

        //when
        NoticeIdAndFileCountResponse response = noticeController.appendFile(noticeId, extraFiles);
        em.flush();
        em.clear();

        //then
        Assertions.assertThat(response.getNoticeId()).isEqualTo(noticeId);
        Assertions.assertThat(response.getFileCnt()).isEqualTo(fileCnt);
        Notice noticeAfterFileAppend = noticeRepository.findDetailById(noticeId).get();
        List<ExtraFile> appendedExtraFiles = noticeAfterFileAppend.getExtraFiles();
        Assertions.assertThat(appendedExtraFiles).hasSize(fileCnt + existCnt);
        int cnt = 0;
        for (ExtraFile appendedExtraFile : appendedExtraFiles) {
            if(appendedExtraFile.getOriginalName().equals("9.pdf")) {
                Assertions.assertThat(amazonS3.doesObjectExist(bucket, appendedExtraFile.getSavedName())).isTrue();
                Assertions.assertThat(appendedExtraFile.getNotice().getId()).isEqualTo(noticeId);
                cnt++;
            }
        }
        Assertions.assertThat(cnt).isEqualTo(fileCnt);
    }

    @Test
    public void appendFile_BadNoticeId_NoticeIdMisMatchException() throws Exception{
        //given
        Long noticeID = -1L;
        int fileCnt = 5;
        List<MultipartFile> extraFiles = readyFiles(fileCnt);

        //when
        assertThrows(
                NoticeIdMisMatchException.class,
                () -> noticeController.appendFile(noticeID, extraFiles));

        //then

    }


    @Test
    public void updateNotice_Default_CheckChangedInfo() throws Exception{
        //given
        Long noticeId = 1L;
        Optional<Notice> notice = noticeRepository.findById(noticeId);
        Assertions.assertThat(notice).isNotEmpty();
        em.clear();
        NoticeCreateRequest noticeCreateRequest = new NoticeCreateRequest("updated title", "updated Content");

        //when
        NoticeIdAndTitleResponse response = noticeController.updateNotice(noticeId, noticeCreateRequest);
        em.flush();
        em.clear();

        //then
        Assertions.assertThat(response.getId()).isEqualTo(noticeId);
        Assertions.assertThat(response.getTitle()).isEqualTo(noticeCreateRequest.getTitle());
        Notice afterChange = noticeRepository.findById(noticeId).get();
        Assertions.assertThat(afterChange.getId()).isEqualTo(noticeId);
        Assertions.assertThat(afterChange.getTitle()).isEqualTo(noticeCreateRequest.getTitle());
        Assertions.assertThat(afterChange.getContent()).isEqualTo(noticeCreateRequest.getContent());
    }

    @Test
    public void updateNotice_BadNoticeId_NoticeIdMistMatchException() throws Exception {
        //given
        Long noticeId = -1L;
        NoticeCreateRequest noticeCreateRequest = new NoticeCreateRequest("updated title", "updated Content");

        //when
        assertThrows(
                NoticeIdMisMatchException.class,
                () -> noticeController.updateNotice(noticeId, noticeCreateRequest));
    }

    @Test
    public void updateThumbnail_FromDefaultThumbnail_NoS3Deletion() throws Exception{
        //given
        String default_key = "default_thumb.png";
        Notice notice = em.createQuery("select n from Notice n inner join n.thumbnail t where t.originalName = :name", Notice.class)
                .setParameter("name", default_key)
                .setMaxResults(1)
                .getSingleResult();
        Long noticeId = notice.getId();
        em.flush();
        em.clear();
        MultipartFile thumbnail = readyThumbnail();

        //when
        NoticeIdAndFileNamesResponse response = noticeController.updateThumbnail(noticeId, thumbnail);
        em.flush();
        em.clear();

        //then
        Assertions.assertThat(response.getNoticeId()).isEqualTo(noticeId);
        Assertions.assertThat(response.getDeletedFileName()).isEqualTo(default_key);
        Assertions.assertThat(amazonS3.doesObjectExist(bucket, default_key)).isTrue();
        Notice afterUpdated = noticeRepository.findById(noticeId).get();
        Assertions.assertThat(afterUpdated.getThumbnail().getOriginalName()).isEqualTo(response.getChangedFileName());
        Assertions.assertThat(amazonS3.doesObjectExist(bucket, afterUpdated.getThumbnail().getUploadedName())).isTrue();

    }

    @Test
    @Commit
    public void updateThumbnail_FromSomeThumbnail_WithS3Deletion() throws Exception{
        //given
        String default_key = "default_thumb.png";
        Notice notice = em.createQuery("select n from Notice n inner join fetch n.thumbnail t where t.originalName <> :name", Notice.class)
                .setParameter("name", default_key)
                .setMaxResults(1)
                .getSingleResult();
        Long noticeId = notice.getId();
        String s3Key = notice.getThumbnail().getUploadedName();
        em.flush();
        em.clear();
        MultipartFile thumbnail = readyThumbnail();

        //when
        NoticeIdAndFileNamesResponse response = noticeController.updateThumbnail(noticeId, thumbnail);
        em.flush();
        em.clear();

        //then
        Assertions.assertThat(response.getNoticeId()).isEqualTo(noticeId);
        Assertions.assertThat(response.getDeletedFileName()).isEqualTo(notice.getThumbnail().getOriginalName());
        Assertions.assertThat(amazonS3.doesObjectExist(bucket, s3Key)).isFalse();
        Notice afterUpdated = noticeRepository.findById(noticeId).get();
        Assertions.assertThat(afterUpdated.getThumbnail().getOriginalName()).isEqualTo(response.getChangedFileName());
        Assertions.assertThat(amazonS3.doesObjectExist(bucket, afterUpdated.getThumbnail().getUploadedName())).isTrue();
    }

    @Test
    public void updateThumbnail_BadNoticeId_NoticeIdMisMatchException() throws Exception{
        //given
        Long noticeId = -1L;
        MultipartFile thumbnail = readyThumbnail();

        //when
        assertThrows(
                NoticeIdMisMatchException.class,
                () -> noticeController.updateThumbnail(noticeId, thumbnail));

        //then

    }


    @Test
    public void deleteNotice_DefaultThumbnail_NoS3Deletion() throws Exception{
        //given
        String default_key = "default_thumb.png";
        Notice notice = em.createQuery("select n from Notice n inner join fetch n.thumbnail t where t.originalName = :name", Notice.class)
                .setParameter("name", default_key)
                .setMaxResults(1)
                .getSingleResult();
        Long noticeId = notice.getId();
        Thumbnail thumbnail = notice.getThumbnail();
        em.clear();

        //when
        NoticeIdAndTitleResponse response = noticeController.deleteNotice(noticeId);
        em.flush();
        em.clear();

        //then
        Assertions.assertThat(response.getId()).isEqualTo(noticeId);
        Assertions.assertThat(response.getTitle()).isEqualTo(notice.getTitle());
        Assertions.assertThat(amazonS3.doesObjectExist(bucket, default_key)).isTrue();
        Optional<Notice> afterDeletion = noticeRepository.findById(noticeId);
        Assertions.assertThat(afterDeletion).isEmpty();
        Assertions.assertThat(em.find(Thumbnail.class, thumbnail.getId())).isNull();
    }

    @Test
    @Commit
    public void deleteNotice_WithThumbnail_S3Deletion() throws Exception{
        //given
        String default_key = "default_thumb.png";
        Notice notice = em.createQuery("select n from Notice n inner join fetch n.thumbnail t where t.originalName <> :name", Notice.class)
                .setParameter("name", default_key)
                .setMaxResults(1)
                .getSingleResult();
        Long noticeId = notice.getId();
        Thumbnail thumbnail = notice.getThumbnail();
        em.clear();

        //when
        NoticeIdAndTitleResponse response = noticeController.deleteNotice(noticeId);
        em.flush();
        em.clear();

        //then
        Assertions.assertThat(response.getId()).isEqualTo(noticeId);
        Assertions.assertThat(response.getTitle()).isEqualTo(notice.getTitle());
        Assertions.assertThat(amazonS3.doesObjectExist(bucket, thumbnail.getUploadedName())).isFalse();
        Optional<Notice> afterDeletion = noticeRepository.findById(noticeId);
        Assertions.assertThat(afterDeletion).isEmpty();
        Assertions.assertThat(em.find(Thumbnail.class, thumbnail.getId())).isNull();
    }

    @Test
    public void deleteNotice_NoExtraFile_SkipS3Deletion() throws Exception{
        //given
        Notice notice = em.createQuery("select n from Notice n left join fetch n.extraFiles e where e.id is null", Notice.class)
                .setMaxResults(1)
                .getSingleResult();
        Long noticeId = notice.getId();
        Assertions.assertThat(notice.getExtraFiles()).isEmpty();
        em.clear();

        //when
        NoticeIdAndTitleResponse response = noticeController.deleteNotice(noticeId);
        em.flush();
        em.clear();

        //then
        Assertions.assertThat(response.getId()).isEqualTo(noticeId);
        Assertions.assertThat(response.getTitle()).isEqualTo(notice.getTitle());
        Optional<Notice> afterDeletion = noticeRepository.findById(noticeId);
        Assertions.assertThat(afterDeletion).isEmpty();
    }

    @Test
    @Commit
    public void deleteNotice_WithExtraFiles_S3BulkDeletion() throws Exception{
        //given
        Notice notice = em.createQuery("select n from Notice n left join fetch n.extraFiles e where e.id is not null", Notice.class)
                .setMaxResults(1)
                .getSingleResult();
        Long noticeId = notice.getId();
        List<ExtraFile> extraFiles = notice.getExtraFiles();
        Assertions.assertThat(extraFiles).isNotEmpty();
        em.clear();

        //when
        NoticeIdAndTitleResponse response = noticeController.deleteNotice(noticeId);
        em.flush();
        em.clear();

        //then
        Assertions.assertThat(response.getId()).isEqualTo(noticeId);
        Assertions.assertThat(response.getTitle()).isEqualTo(notice.getTitle());
        Optional<Notice> afterDeletion = noticeRepository.findById(noticeId);
        Assertions.assertThat(afterDeletion).isEmpty();
        List<ExtraFile> findedExtraFiles = extraFileRepository.findAllById(extraFiles.stream().map(ExtraFile::getId).collect(Collectors.toList()));
        Assertions.assertThat(findedExtraFiles).isEmpty();
        for (ExtraFile extraFile : extraFiles) {
            Assertions.assertThat(amazonS3.doesObjectExist(bucket, extraFile.getSavedName())).isFalse();
        }
    }

    @Test
    public void deleteNotice_BadNoticeId_NoticeIdMistMatchException() throws Exception{
        //given
        Long noticeId = -1L;

        //when
        assertThrows(
                NoticeIdMisMatchException.class,
                () -> noticeController.deleteNotice(noticeId));

        //then

    }

    @Test
    @Commit
    public void deleteFileByOriginalName_Default_CannotFindFromNotice() throws Exception{
        //given
        String fileName = "3.pdf";
        ExtraFile extraFile = em.createQuery("select e from ExtraFile e where e.originalName = :name", ExtraFile.class)
                .setParameter("name", fileName)
                .setMaxResults(1)
                .getSingleResult();
        Long noticeId = extraFile.getNotice().getId();
        em.clear();

        //when
        NoticeIdAndDeletedNameResponse response = noticeController.deleteFileByOriginalName(noticeId, fileName);
        em.flush();
        em.clear();

        //then
        Assertions.assertThat(response.getNoticeId()).isEqualTo(noticeId);
        Assertions.assertThat(response.getDeletedFileName()).isEqualTo(fileName);
        Assertions.assertThat(amazonS3.doesObjectExist(bucket, extraFile.getSavedName())).isFalse();
        Assertions.assertThat(em.find(ExtraFile.class, extraFile.getId())).isNull();
        Notice afterFileDeletion = noticeRepository.findDetailById(noticeId).get();
        Assertions.assertThat(afterFileDeletion.getExtraFiles()).doesNotContain(extraFile);
        for (ExtraFile file : afterFileDeletion.getExtraFiles()) {
            log.info("file.getOriginalName() : {}", file.getOriginalName());
        }
    }

    @Test
    public void deleteFileByOriginalName_CannotFindFileInNotice_ExtraFileNameMisMatchException() throws Exception{
        //given
        String fileName = "3.pdf";
        ExtraFile extraFile = em.createQuery("select e from ExtraFile e group by e.notice.id having count(e.id) < 3", ExtraFile.class)
                .setMaxResults(1)
                .getSingleResult();
        Long noticeId = extraFile.getNotice().getId();
        em.clear();

        //when
        assertThrows(
                ExtraFileNameMisMatchException.class,
                () -> noticeController.deleteFileByOriginalName(noticeId, fileName));

        //then

    }

    @Test
    public void deleteFileByOriginalName_BadNoticeID_NoticeIdMisMatchException() throws Exception{
        //given
        Long noticeId = -1L;
        String fileName = "0.pdf";

        //when
        assertThrows(
                NoticeIdMisMatchException.class,
                () -> noticeController.deleteFileByOriginalName(noticeId, fileName));

        //then

    }

    private User readyUser() {
        return new User(
                "commandUserId",
                "commandUserPW",
                Role.ROLE_ADMIN_SEOUL_CENTRAL,
                "command test User",
                "command test Contact"
        );
    }

    private Notice readyNotice(User user, Thumbnail thumbnail) {
        return new Notice(
                "command test title",
                "command test content",
                user,
                thumbnail
        );
    }

    private MultipartFile readyThumbnail() throws IOException {
        Path path = Paths.get("src/test/resources/img/6.jpg");
        byte[] bytes = Files.readAllBytes(path);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        return new MockMultipartFile("thumbnail", "6.jpg", "image/jpeg", byteArrayInputStream);
    }

    private List<MultipartFile> readyFiles(int index) throws IOException {
        List<MultipartFile> files = new ArrayList<>();
        for (int i = 0; i < index; i++) {
            Path path = Paths.get("src/test/resources/file/9.pdf");
            byte[] bytes = Files.readAllBytes(path);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            files.add(new MockMultipartFile("files", "9.pdf", "application/pdf", byteArrayInputStream));
        }
        return files;
    }
}
