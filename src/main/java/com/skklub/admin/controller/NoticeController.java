package com.skklub.admin.controller;

import com.skklub.admin.controller.dto.*;
import com.skklub.admin.domain.ExtraFile;
import com.skklub.admin.domain.Notice;
import com.skklub.admin.domain.Thumbnail;
import com.skklub.admin.domain.enums.Role;
import com.skklub.admin.exception.deprecated.error.exception.CannotCategorizeByMasterException;
import com.skklub.admin.exception.deprecated.error.exception.CannotCategorizeByUserException;
import com.skklub.admin.exception.deprecated.error.exception.ExtraFileNameMisMatchException;
import com.skklub.admin.exception.deprecated.error.exception.NoticeIdMisMatchException;
import com.skklub.admin.repository.NoticeRepository;
import com.skklub.admin.security.jwt.TokenProvider;
import com.skklub.admin.service.NoticeService;
import com.skklub.admin.service.dto.FileNames;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
public class NoticeController {

    private final static String DEFAULT_THUMBNAIL = "default_thumb.png";
    private final S3Transferer s3Transferer;
    private final NoticeService noticeService;
    private final NoticeRepository noticeRepository;
    private final AuthValidator authValidator;

//=====CREATE=====//

    //등록
    @PostMapping("/notice")
    public NoticeIdAndTitleResponse createNotice(@ModelAttribute @Valid NoticeCreateRequest noticeCreateRequest
            , @RequestParam(required = false) MultipartFile thumbnailFile
            ,@RequestParam Optional<List<MultipartFile>> files
            , @AuthenticationPrincipal UserDetails userDetails) {
        Thumbnail thumbnail = Optional.ofNullable(thumbnailFile).map(s3Transferer::uploadOne).orElse(new FileNames(DEFAULT_THUMBNAIL, DEFAULT_THUMBNAIL)).toThumbnailEntity();
        List<ExtraFile> extraFiles = s3Transferer.uploadAll(files.orElse(new ArrayList<>()))
                .stream()
                .map(FileNames::toExtraFileEntity)
                .collect(Collectors.toList());
        String userName = TokenProvider.getAuthentication(userDetails).getName();
        Long noticeId = noticeService.createNotice(noticeCreateRequest.getTitle(), noticeCreateRequest.getContent(), userName, thumbnail, extraFiles);
        return new NoticeIdAndTitleResponse(noticeId, noticeCreateRequest.getTitle());
    }

    //파일 등록
    @PostMapping("/notice/{noticeId}/file")
    public NoticeIdAndFileCountResponse appendFile(@PathVariable Long noticeId, @RequestParam List<MultipartFile> files) {
        return noticeRepository.findById(noticeId).map(notice -> {
            List<ExtraFile> extraFiles = s3Transferer.uploadAll(files).stream().map(FileNames::toExtraFileEntity).collect(Collectors.toList());
            int fileCnt = noticeService.appendExtraFiles(notice, extraFiles);
            return new NoticeIdAndFileCountResponse(noticeId, fileCnt);
        }).orElseThrow(NoticeIdMisMatchException::new);
    }

//=====READ=====//

    //세부 조회
    @GetMapping("/notice/{noticeId}")
    public NoticeDetailResponse getDetailNotice(@PathVariable Long noticeId) {
        Notice notice = noticeRepository.findDetailById(noticeId).orElseThrow(NoticeIdMisMatchException::new);
        List<FileNames> fileNames = notice.getExtraFiles().stream().map(FileNames::new).collect(Collectors.toList());
        List<S3DownloadDto> s3DownloadDtos = s3Transferer.downloadAll(fileNames);
        log.info("s3DownloadDtos.size() : {}", s3DownloadDtos.size());
        Optional<Notice> preNotice = noticeService.findPreNotice(notice);
        Optional<Notice> postNotice = noticeService.findPostNotice(notice);
        return new NoticeDetailResponse(notice, s3DownloadDtos, preNotice, postNotice);
    }

    //파일 조회
//    @GetMapping("/notice/file")
//    public ResponseEntity<byte[]> getFile(@RequestParam String fileSavedName) {
//        S3DownloadDto s3DownloadDto = s3Transferer.downloadOne(new FileNames(null, fileSavedName));
//        HttpHeaders httpHeaders = new HttpHeaders();
//        httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
//        httpHeaders.setContentLength(s3DownloadDto.getBytes().length);
//        return new ResponseEntity<>(s3DownloadDto.getBytes(), httpHeaders, HttpStatus.OK);
//    }

    //목록 조회(with 썸네일)
    @GetMapping("/notice/prev/thumbnail")
    public Page<NoticePrevWithThumbnailResponse> getNoticePrevWithThumbnail(Pageable pageable) {
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort().and(Sort.by("createdAt").ascending()));
        return noticeRepository.findAllWithThumbnailBy(pageRequest).map(notice -> {
            Thumbnail thumbnail = notice.getThumbnail();
            S3DownloadDto s3DownloadDto = s3Transferer.downloadOne(new FileNames(thumbnail));
            return new NoticePrevWithThumbnailResponse(notice, s3DownloadDto);
        });
    }

    @GetMapping("/notice/prev/{noticeId}")
    public NoticePrevWithThumbnailResponse getNoticeThumbnailByNoticeId(@PathVariable Long noticeId) {
        Notice notice = noticeRepository.findWithThumbnailById(noticeId).orElseThrow(NoticeIdMisMatchException::new);
        S3DownloadDto s3DownloadDto = s3Transferer.downloadOne(new FileNames(notice.getThumbnail()));
        return new NoticePrevWithThumbnailResponse(notice, s3DownloadDto);
    }


    //목록 조회(전체(작성자 선택), 시간순)
    @GetMapping("/notice/prev")
    public Page<NoticePrevResponse> getNoticePrev(@RequestParam(required = false) Optional<Role> role, Pageable pageable) {
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort().and(Sort.by("createdAt").ascending()));
        Page<Notice> notices = role.map(r -> {
            if (r.equals(Role.ROLE_MASTER)) throw new CannotCategorizeByMasterException();
            if (r.equals(Role.ROLE_USER)) throw new CannotCategorizeByUserException();
            return noticeRepository.findAllByUserRole(r, pageRequest);
        }).orElseGet(() -> noticeRepository.findAll(pageRequest));
        return notices.map(NoticePrevResponse::new);
    }

    //목록 조회(제목 검색, 시간순)
    @GetMapping("/notice/prev/search/title")
    public Page<NoticePrevResponse> getNoticePrevByTitle(@RequestParam String title, Pageable pageable) {
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort().and(Sort.by("createdAt").ascending()));
        return noticeRepository.findWithWriterAllByTitleContainingOrderByCreatedAt(title, pageRequest).map(NoticePrevResponse::new);
    }

//=====UPDATE=====//

    //내용 수정
    @PatchMapping("/notice/{noticeId}")
    public NoticeIdAndTitleResponse updateNotice(@PathVariable Long noticeId, @ModelAttribute @Valid NoticeCreateRequest noticeCreateRequest) {
        authValidator.validateUpdatingNotice(noticeId);
        Notice updateInfo = noticeCreateRequest.toEntity();
        return noticeService.updateNotice(noticeId, updateInfo).map(title -> new NoticeIdAndTitleResponse(noticeId, title)).orElseThrow(NoticeIdMisMatchException::new);
    }

    //썸네일(로고) 변경
    @PostMapping("/notice/{noticeId}/thumbnail")
    public NoticeIdAndFileNamesResponse updateThumbnail(@PathVariable Long noticeId, @RequestParam MultipartFile thumbnailFile) {
        if (!noticeRepository.existsById(noticeId)) throw new NoticeIdMisMatchException();
        authValidator.validateUpdatingNotice(noticeId);
        FileNames thumbnailFileName = s3Transferer.uploadOne(thumbnailFile);
        Thumbnail thumbnail = thumbnailFileName.toThumbnailEntity();
        return noticeService.updateThumbnail(noticeId, thumbnail).map(oldThumbnailFileName -> {
            if (!oldThumbnailFileName.getSavedName().equals(DEFAULT_THUMBNAIL))
                s3Transferer.deleteOne(oldThumbnailFileName.getSavedName());
            return new NoticeIdAndFileNamesResponse(noticeId, oldThumbnailFileName.getOriginalName(), thumbnailFileName.getOriginalName());
        }).orElseThrow(NoticeIdMisMatchException::new);
    }

//=====DELETE=====//

    //삭제
    @DeleteMapping("/notice/{noticeId}")
    public NoticeIdAndTitleResponse deleteNotice(@PathVariable Long noticeId) {
        authValidator.validateUpdatingNotice(noticeId);
        return noticeService.deleteNotice(noticeId).map(noticeDeletionDto -> {
            FileNames thumbnailFileName = noticeDeletionDto.getThumbnailFileName();
            if (!thumbnailFileName.getSavedName().equals(DEFAULT_THUMBNAIL))
                s3Transferer.deleteOne(thumbnailFileName.getSavedName());
            List<String> extraFileKeys = noticeDeletionDto.getExtraFileNames().stream().map(FileNames::getSavedName).collect(Collectors.toList());
            s3Transferer.deleteAll(extraFileKeys);
            return new NoticeIdAndTitleResponse(noticeId, noticeDeletionDto.getNoticeTitle());
        }).orElseThrow(NoticeIdMisMatchException::new);
    }

    //특정 파일 삭제
    @DeleteMapping("/notice/{noticeId}/{fileName}")
    public NoticeIdAndDeletedNameResponse deleteFileByOriginalName(@PathVariable Long noticeId, @PathVariable String fileName) {
        authValidator.validateUpdatingNotice(noticeId);
        return noticeService.deleteExtraFile(noticeId, fileName).map(deletedExtraFileNames -> {
            s3Transferer.deleteOne(deletedExtraFileNames.getSavedName());
            return new NoticeIdAndDeletedNameResponse(noticeId, deletedExtraFileNames.getOriginalName());
        }).orElseThrow(ExtraFileNameMisMatchException::new);
    }
}
