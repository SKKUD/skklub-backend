package com.skklub.admin.controller;

import com.skklub.admin.service.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequestMapping
@RestController
@RequiredArgsConstructor
public class NoticeFIleController {
    private final S3Uploader s3Uploader;

    //공지사항 썸네일 등록

    //공지사항 썸네일 조회

    //공지사항 내부 파일 등록

    //공지사항 내부 파일 조회

    //공지사항 관련 모든 파일 삭제
}
