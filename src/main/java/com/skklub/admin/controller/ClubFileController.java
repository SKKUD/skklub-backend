package com.skklub.admin.controller;

import com.skklub.admin.service.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/club")
public class ClubFileController {
    private final S3Uploader s3Uploader;

//    //로고 이미지 등록(C)
//    @PostMapping("/logo/{clubName}")
//    public String createClubLogo(@RequestParam MultipartFile multipartFile, @PathVariable String clubName) {
//        return s3Uploader.uploadClubLogo(multipartFile, clubName);
//    }
//
//    //로고 이미지 수정(U)
//    @PatchMapping("/logo/{clubName}")
//    public String updateClubLogo(@RequestParam MultipartFile multipartFile, @PathVariable String clubName) {
//        return s3Uploader.update(multipartFile, clubName);
//    }
//
//    //로고 이미지 조회(R)
//    @GetMapping("/logo/{clubName}")
//    public MultipartFile getClubLogo(@RequestParam String clubName) {
//
//    }
//
//    //로고 이미지 삭제(D)
//    @DeleteMapping("/logo/{clubName}")
//    public String deleteClubLogo(@RequestParam String clubName) {
//
//    }
//
//    //활동 사진 추가(C)
//    @PostMapping("/activityImage/{clubName}")
//    public String createActivityImage(@RequestParam MultipartFile multipartFile, @PathVariable String clubName) {
//        return s3Uploader.appendClubActivityImage(multipartFile, clubName);
//    }
//
//    //활동 사진 수정(U)
//    @PatchMapping("/activityImage/{clubName}")
//    public String updateActivityImage(@RequestParam MultipartFile multipartFile, @PathVariable String clubName) {
//        return s3Uploader.update(multipartFile, clubName);
//    }
//
//    //활동 사진 전체 조회(R)
//    @GetMapping("/activityImage/{clubName}")
//    public MultipartFile getActivityImage(@RequestParam String clubName) {
//
//    }
//
//    //활동 사진 하나만 조
//    @GetMapping("/")
//
//    //활동 사진 삭제(D) - One
//    @DeleteMapping("/activityImage/{fileName}")
//    public String deleteActivityImage(@RequestParam String fileName) {
//
//    }
//
//    //화동 사진 삭제(D) - 전체
//    @DeleteMapping("/activityImage/{clubName}/all")
//    public
}
