package com.skklub.admin.controller;

import com.skklub.admin.controller.dto.NoticeCreateRequest;
import com.skklub.admin.controller.dto.NoticeIdAndFileCountResponse;
import com.skklub.admin.controller.dto.NoticeIdAndTitleResponse;
import com.skklub.admin.domain.ExtraFile;
import com.skklub.admin.domain.Thumbnail;
import com.skklub.admin.error.exception.NoticeIdMisMatchException;
import com.skklub.admin.repository.NoticeRepository;
import com.skklub.admin.security.jwt.TokenProvider;
import com.skklub.admin.service.NoticeService;
import com.skklub.admin.service.dto.FileNames;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
public class NoticeController {

    private final S3Transferer s3Transferer;
    private final NoticeService noticeService;
    private final NoticeRepository noticeRepository;

//=====CREATE=====//

    //등록
    @PostMapping("/notice")
    public NoticeIdAndTitleResponse createNotice(@ModelAttribute NoticeCreateRequest noticeCreateRequest,
                                                                 @RequestParam(required = false) MultipartFile thumbnailFile,
                                                                 @AuthenticationPrincipal UserDetails userDetails){
        Thumbnail thumbnail = Optional.ofNullable(thumbnailFile)
                .map(s3Transferer::uploadOne)
                .orElse(new FileNames("default_thumb.png", "default_thumb.png"))
                .toThumbnailEntity();
        String userName = TokenProvider.getAuthentication(userDetails).getName();
        Long noticeId = noticeService.createNotice(noticeCreateRequest.getTitle(), noticeCreateRequest.getContent(), userName, thumbnail);
        return new NoticeIdAndTitleResponse(noticeId, noticeCreateRequest.getTitle());
    }

    //파일 등록
    @PostMapping("/notice/{noticeId}/file")
    public NoticeIdAndFileCountResponse appendFile(@PathVariable Long noticeId, @RequestParam List<MultipartFile> files) {
        return noticeRepository.findById(noticeId)
                .map(
                        notice -> {
                            List<ExtraFile> extraFiles = s3Transferer.uploadAll(files).stream()
                                    .map(FileNames::toExtraFileEntity)
                                    .collect(Collectors.toList());
                            int fileCnt = noticeService.appendExtraFiles(notice, extraFiles);
                            return new NoticeIdAndFileCountResponse(noticeId, fileCnt);
                        }
                )
                .orElseThrow(NoticeIdMisMatchException::new);
    }

//=====READ=====//

    //세부 조회

    //파일 조회

    //목록 조회(with 썸네일)

    //목록 조회(전체, 시간순)

    //목록 조회(제목 검색, 시간순)

    //목록 조회(작성자 검색, 시간순)

//=====UPDATE=====//

    //내용 수정

    //썸네일(로고) 변경

//=====DELETE=====//

    //삭제

    //특정 파일 삭제

}
