package com.skklub.admin.controller;

import com.skklub.admin.service.ClubService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/club")
public class ClubController {
    private ClubService clubService;

    //동아리 조회 by ID

    //동아리 조회 by Name

    //동아리 조회 by Campus

    //동아리 조회 by ActivityType

    //동아리 추가

    //로고 사진 삽입

    //활동 사진 추가

    //

}
