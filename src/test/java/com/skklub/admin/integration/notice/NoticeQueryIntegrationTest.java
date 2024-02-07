package com.skklub.admin.integration.notice;

import com.skklub.admin.WithMockCustomUser;
import com.skklub.admin.controller.NoticeController;
import com.skklub.admin.controller.S3Transferer;
import com.skklub.admin.controller.dto.*;
import com.skklub.admin.domain.ExtraFile;
import com.skklub.admin.domain.Notice;
import com.skklub.admin.domain.User;
import com.skklub.admin.domain.enums.Role;
import com.skklub.admin.exception.deprecated.error.exception.CannotCategorizeByMasterException;
import com.skklub.admin.exception.deprecated.error.exception.CannotCategorizeByUserException;
import com.skklub.admin.repository.ExtraFileRepository;
import com.skklub.admin.repository.NoticeRepository;
import com.skklub.admin.service.dto.FileNames;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
@SpringBootTest
@Transactional
@WithMockCustomUser(username = "testMasterID",role = Role.ROLE_MASTER)
public class NoticeQueryIntegrationTest {
    @Autowired
    private EntityManager em;
    @Autowired
    private NoticeController noticeController;
    @Autowired
    private NoticeRepository noticeRepository;
    @Autowired
    private ExtraFileRepository extraFileRepository;
    @Autowired
    private S3Transferer s3Transferer;

    @Test
    public void getDetailNotice_WithPreAndPost_ReturnBoth() throws Exception{
        //given
        List<Notice> notices = em.createQuery("select n from Notice n inner join fetch n.writer order by n.createdAt asc", Notice.class)
                .getResultList();
        Assertions.assertThat(notices.size()).isGreaterThan(2);
        int noticeIndex = notices.size() / 2;
        Notice notice = notices.get(noticeIndex);
        User writer = notice.getWriter();
        List<ExtraFile> extraFiles = em.createQuery("select e from ExtraFile e where e.notice = :notice")
                .setParameter("notice", notice)
                .getResultList();
        Long noticeId = notice.getId();
        em.clear();

        //when
        NoticeDetailResponse response = noticeController.getDetailNotice(noticeId);
        em.clear();

        //then
        Assertions.assertThat(response.getNoticeId()).isEqualTo(noticeId);
        Assertions.assertThat(response.getTitle()).isEqualTo(notice.getTitle());
        Assertions.assertThat(response.getContent()).isEqualTo(notice.getContent());
        Assertions.assertThat(response.getCreatedAt()).isEqualTo(notice.getCreatedAt());
        Assertions.assertThat(response.getWriterName()).isEqualTo(writer.getName());
        Assertions.assertThat(response.getExtraFileDownloadDtos()).hasSize(extraFiles.size());
        Assertions.assertThat(response.getExtraFileDownloadDtos()).containsAll(
                extraFiles.stream().map(FileNames::new).map(s3Transferer::downloadOne).collect(Collectors.toList())
        );
        Assertions.assertThat(response.getPreNotice()).isNotEmpty();
        NoticeIdAndTitleResponse preNotice = response.getPreNotice().get();
        Assertions.assertThat(preNotice.getId()).isEqualTo(notices.get(noticeIndex - 1).getId());
        Assertions.assertThat(preNotice.getTitle()).isEqualTo(notices.get(noticeIndex - 1).getTitle());

        Assertions.assertThat(response.getPostNotice()).isNotEmpty();
        NoticeIdAndTitleResponse postNotice = response.getPostNotice().get();
        Assertions.assertThat(postNotice.getId()).isEqualTo(notices.get(noticeIndex + 1).getId());
        Assertions.assertThat(postNotice.getTitle()).isEqualTo(notices.get(noticeIndex + 1).getTitle());
    }

    @Test
    public void getDetailNotice_NoPre_ReturnNullInPre() throws Exception{
        //given
        List<Notice> notices = em.createQuery("select n from Notice n inner join fetch n.writer order by n.createdAt asc", Notice.class)
                .getResultList();
        Assertions.assertThat(notices.size()).isGreaterThan(2);
        int noticeIndex = 0;
        Notice notice = notices.get(noticeIndex);
        User writer = notice.getWriter();
        List<ExtraFile> extraFiles = em.createQuery("select e from ExtraFile e where e.notice = :notice")
                .setParameter("notice", notice)
                .getResultList();
        Long noticeId = notice.getId();
        em.clear();

        //when
        NoticeDetailResponse response = noticeController.getDetailNotice(noticeId);
        em.clear();

        //then
        Assertions.assertThat(response.getNoticeId()).isEqualTo(noticeId);
        Assertions.assertThat(response.getTitle()).isEqualTo(notice.getTitle());
        Assertions.assertThat(response.getContent()).isEqualTo(notice.getContent());
        Assertions.assertThat(response.getCreatedAt()).isEqualTo(notice.getCreatedAt());
        Assertions.assertThat(response.getWriterName()).isEqualTo(writer.getName());
        Assertions.assertThat(response.getExtraFileDownloadDtos()).hasSize(extraFiles.size());
        Assertions.assertThat(response.getExtraFileDownloadDtos()).containsAll(
                extraFiles.stream().map(FileNames::new).map(s3Transferer::downloadOne).collect(Collectors.toList())
        );
        Assertions.assertThat(response.getPreNotice()).isEmpty();

        Assertions.assertThat(response.getPostNotice()).isNotEmpty();
        NoticeIdAndTitleResponse postNotice = response.getPostNotice().get();
        Assertions.assertThat(postNotice.getId()).isEqualTo(notices.get(noticeIndex + 1).getId());
        Assertions.assertThat(postNotice.getTitle()).isEqualTo(notices.get(noticeIndex + 1).getTitle());
    }

    @Test
    public void getDetailNotice_NoPreAndPost_ReturnNullInBoth() throws Exception{
        //given
        List<Notice> notices = em.createQuery("select n from Notice n inner join fetch n.writer order by n.createdAt asc", Notice.class)
                .getResultList();
        noticeRepository.deleteAll(notices.subList(1, notices.size()));
        em.flush();
        em.clear();
        int noticeIndex = 0;
        Notice notice = notices.get(noticeIndex);
        User writer = notice.getWriter();
        List<ExtraFile> extraFiles = em.createQuery("select e from ExtraFile e where e.notice = :notice")
                .setParameter("notice", notice)
                .getResultList();
        Long noticeId = notice.getId();
        em.clear();

        //when
        NoticeDetailResponse response = noticeController.getDetailNotice(noticeId);
        em.clear();

        //then
        Assertions.assertThat(response.getNoticeId()).isEqualTo(noticeId);
        Assertions.assertThat(response.getTitle()).isEqualTo(notice.getTitle());
        Assertions.assertThat(response.getContent()).isEqualTo(notice.getContent());
        Assertions.assertThat(response.getCreatedAt()).isEqualTo(notice.getCreatedAt());
        Assertions.assertThat(response.getWriterName()).isEqualTo(writer.getName());
        Assertions.assertThat(response.getExtraFileDownloadDtos()).hasSize(extraFiles.size());
        Assertions.assertThat(response.getExtraFileDownloadDtos()).containsAll(
                extraFiles.stream().map(FileNames::new).map(s3Transferer::downloadOne).collect(Collectors.toList())
        );
        Assertions.assertThat(response.getPreNotice()).isEmpty();

        Assertions.assertThat(response.getPostNotice()).isEmpty();
    }
    
//    @Test
//    public void getFile_Default_CheckHeaderAndBody() throws Exception{
//        //given
//        ExtraFile extraFile = em.createQuery("select e from ExtraFile e", ExtraFile.class)
//                .setMaxResults(1)
//                .getSingleResult();
//        em.clear();
//
//        //when
//        ResponseEntity<byte[]> response = noticeController.getFile(extraFile.getSavedName());
//
//        //then
//        Assertions.assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_OCTET_STREAM);
//        Assertions.assertThat(response.getHeaders().getContentLength()).isNotZero();
//    }
    
    @Test
    public void getNoticePrevWithThumbnail_Default_FindDefaultOrSomeThumbnail() throws Exception{
        //given
        PageRequest request = PageRequest.of(1, 5);
        Page<Notice> notices = noticeRepository.findAllWithThumbnailBy(request);
        em.clear();

        //when
        Page<NoticePrevWithThumbnailResponse> response = noticeController.getNoticePrevWithThumbnail(request);

        //then
        Assertions.assertThat(response.getTotalElements()).isEqualTo(notices.getTotalElements());
        Assertions.assertThat(response.getTotalPages()).isEqualTo(notices.getTotalPages());
        Assertions.assertThat(response.getNumber()).isEqualTo(notices.getNumber());
        Assertions.assertThat(response.getNumberOfElements()).isEqualTo(notices.getNumberOfElements());
        Assertions.assertThat(response.getSize()).isEqualTo(notices.getSize());
        Assertions.assertThat(response.getSort()).isEqualTo(Sort.by("createdAt").ascending());
        for (NoticePrevWithThumbnailResponse r : response) {
            Assertions.assertThat(notices.stream().map(Notice::getId)).contains(r.getNoticeId());
            Assertions.assertThat(notices.stream().map(Notice::getContent)).contains(r.getContent());
            Assertions.assertThat(notices.stream().map(Notice::getTitle)).contains(r.getTitle());
            Assertions.assertThat(notices.stream().map(Notice::getCreatedAt)).contains(r.getCreatedAt());
        }
        List<S3DownloadDto> s3DownloadDtos = notices.stream()
                .map(Notice::getThumbnail)
                .map(FileNames::new)
                .map(s3Transferer::downloadOne)
                .collect(Collectors.toList());
        List<Long> ids = s3DownloadDtos.stream().map(S3DownloadDto::getId).collect(Collectors.toList());
        List<String> urls = s3DownloadDtos.stream().map(S3DownloadDto::getUrl).collect(Collectors.toList());
        List<String> fileNames = s3DownloadDtos.stream().map(S3DownloadDto::getFileName).collect(Collectors.toList());

        response.map(NoticePrevWithThumbnailResponse::getThumbnail)
                .stream()
                .forEach(
                        s3DownloadDto -> {
                            Assertions.assertThat(ids).contains(s3DownloadDto.getId());
                            Assertions.assertThat(urls).contains(s3DownloadDto.getUrl());
                            Assertions.assertThat(fileNames).contains(s3DownloadDto.getFileName());
                        }
                );
    }

    @Test
    public void getNoticePrev_findAll_Success() throws Exception{
        //given
        PageRequest request = PageRequest.of(1, 3);
        Page<Notice> notices = noticeRepository.findAll(request);
        em.clear();
        Optional<Role> role = Optional.empty();

        //when
        Page<NoticePrevResponse> response = noticeController.getNoticePrev(role, request);
        em.clear();

        //then
        Assertions.assertThat(response.getTotalElements()).isEqualTo(notices.getTotalElements());
        Assertions.assertThat(response.getTotalPages()).isEqualTo(notices.getTotalPages());
        Assertions.assertThat(response.getNumber()).isEqualTo(notices.getNumber());
        Assertions.assertThat(response.getNumberOfElements()).isEqualTo(notices.getNumberOfElements());
        Assertions.assertThat(response.getSize()).isEqualTo(notices.getSize());
        Assertions.assertThat(response.getSort()).isEqualTo(Sort.by("createdAt").ascending());
        for (NoticePrevResponse r : response) {
            Assertions.assertThat(notices.stream().map(Notice::getId)).contains(r.getNoticeId());
            Assertions.assertThat(notices.stream().map(Notice::getTitle)).contains(r.getTitle());
            Assertions.assertThat(notices.stream().map(Notice::getWriter).map(User::getName)).contains(r.getWriterName());
            Assertions.assertThat(notices.stream().map(Notice::getCreatedAt)).contains(r.getCreatedAt());
        }

    }

    @Test
    public void getNoticePrev_SearchBySomeAdminRole_Success() throws Exception{
        //given
        PageRequest request = PageRequest.of(1, 3);
        Optional<Role> role = Optional.of(Role.ROLE_ADMIN_SEOUL_CENTRAL);
        Page<Notice> notices = noticeRepository.findAllByUserRole(role.get(), request);
        notices.map(n -> n.getWriter().getRole())
                        .forEach(r -> Assertions.assertThat(r).isEqualTo(Role.ROLE_ADMIN_SEOUL_CENTRAL));
        em.clear();

        //when
        Page<NoticePrevResponse> response = noticeController.getNoticePrev(role, request);
        em.clear();

        //then
        Assertions.assertThat(response.getTotalElements()).isEqualTo(notices.getTotalElements());
        Assertions.assertThat(response.getTotalPages()).isEqualTo(notices.getTotalPages());
        Assertions.assertThat(response.getNumber()).isEqualTo(notices.getNumber());
        Assertions.assertThat(response.getNumberOfElements()).isEqualTo(notices.getNumberOfElements());
        Assertions.assertThat(response.getSize()).isEqualTo(notices.getSize());
        Assertions.assertThat(response.getSort()).isEqualTo(Sort.by("createdAt").ascending());
        for (NoticePrevResponse r : response) {
            Assertions.assertThat(notices.stream().map(Notice::getId)).contains(r.getNoticeId());
            Assertions.assertThat(notices.stream().map(Notice::getTitle)).contains(r.getTitle());
            Assertions.assertThat(notices.stream().map(Notice::getWriter).map(User::getName)).contains(r.getWriterName());
            Assertions.assertThat(notices.stream().map(Notice::getCreatedAt)).contains(r.getCreatedAt());
        }

    }

    @Test
    public void getNoticePrev_SearchByUserRole_CannotCategorizeByUserException() throws Exception{
        //given
        Optional<Role> role = Optional.of(Role.ROLE_USER);
        PageRequest request = PageRequest.of(1, 3);

        //when
        assertThrows(
                CannotCategorizeByUserException.class,
                () -> noticeController.getNoticePrev(role, request)
        );

        //then

    }

    @Test
    public void getNoticePrev_SearchByMasterRole_CannotCategorizeByMasterException() throws Exception{
        //given
        Optional<Role> role = Optional.of(Role.ROLE_MASTER);
        PageRequest request = PageRequest.of(1, 3);

        //when
        assertThrows(
                CannotCategorizeByMasterException.class,
                () -> noticeController.getNoticePrev(role, request)
        );
        //then

    }
    
    @Test
    public void getNoticePrevByTitle_FirstMiddleLastMatch_FindAll() throws Exception{
        //given
        String keyword = " 1";
        PageRequest request = PageRequest.of(1, 3);
        List<Notice> notices = em.createQuery("select n from Notice n where n.title like :keyword order by n.createdAt asc", Notice.class)
                .setParameter("keyword", "%" + keyword + "%")
                .getResultList();
        em.clear();

        //when
        Page<NoticePrevResponse> response = noticeController.getNoticePrevByTitle(keyword, request);

        //then
        Assertions.assertThat(response.getTotalPages()).isEqualTo((int) Math.ceil((double)notices.size() / 3));
        Assertions.assertThat(response.getTotalElements()).isEqualTo(notices.size());
        Assertions.assertThat(response.getNumber()).isEqualTo(request.getPageNumber());
        Assertions.assertThat(response.getNumberOfElements()).isEqualTo(request.getPageSize());
        Assertions.assertThat(response.getSort()).isEqualTo(Sort.by("createdAt").ascending());
    }
}
