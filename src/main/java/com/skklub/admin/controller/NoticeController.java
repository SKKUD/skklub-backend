package com.skklub.admin.controller;

import com.skklub.admin.controller.dto.NoticeIdAndTitleResponse;
import com.skklub.admin.controller.dto.NoticeWriteRequest;
import com.skklub.admin.domain.ExtraFile;
import com.skklub.admin.service.dto.FileNames;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
public class NoticeController {

    private final S3Transferer s3Transferer;

//=====CREATE=====//

    //등록

    //파일 등록

//=====READ=====//

    //세부 조회

    //파일 조회회

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
