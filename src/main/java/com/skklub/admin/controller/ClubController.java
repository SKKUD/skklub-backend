package com.skklub.admin.controller;

import com.skklub.admin.controller.dto.*;
import com.skklub.admin.domain.Club;
import com.skklub.admin.service.ClubService;
import com.skklub.admin.service.dto.ClubDetailInfoDto;
import com.skklub.admin.service.dto.FileNames;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ClubController {

    private final ClubService clubService;
    private final S3Transferer s3Transferer;

//=====CREATE=====//

    //추가
    @PostMapping(value = "/club", produces = "application/json")
    public ClubNameAndIdDTO createClub(@ModelAttribute ClubCreateRequestDTO clubCreateRequestDTO, @RequestParam MultipartFile logo) {
        log.info("club name : {}, logo size : {}", clubCreateRequestDTO.getClubName(), logo.getSize());
        FileNames uploadedLogo = s3Transferer.uploadOne(logo);
        Club club = clubCreateRequestDTO.toEntity();
        Long id = clubService.createClub(club, uploadedLogo.getOriginalName(), uploadedLogo.getSavedName());
        return new ClubNameAndIdDTO(id, club.getName());
    }

    //활동 사진 등록(LIST)
    @PostMapping("/club/{clubId}/activityImage")
    public ClubNameAndIdDTO uploadActivityImages(@PathVariable Long clubId, @RequestParam List<MultipartFile> activityImages) {
        log.info("request CLUB : {}, file count : {}", clubId, activityImages.size());
        List<FileNames> savedActivityImages = s3Transferer.uploadAll(activityImages);
        String clubName = clubService.appendActivityImages(clubId, savedActivityImages);
        return new ClubNameAndIdDTO(clubId, clubName);
    }

//=====READ=====//

    //세부 정보 조회 by ID
    @GetMapping(value = "/club/{clubId}")
    public ClubResponseDTO getClubById(@PathVariable Long clubId) {
        log.info("request CLUB Id : {}", clubId);
        ClubDetailInfoDto clubDetailInfoDto = clubService.getClubDetailInfo(clubId);
        S3DownloadDto logo = s3Transferer.downloadOne(clubDetailInfoDto.getLogo());
        List<S3DownloadDto> activityImages = s3Transferer.downloadAll(clubDetailInfoDto.getActivityImages());
        return new ClubResponseDTO(clubDetailInfoDto, logo, activityImages);
    }
//
//    //간소화(Preview) 조회
//    @GetMapping("/club/prev")
//    public Page<ClubPrevDTO> getClubPrev(Pageable pageable) {
//        return null;
//    }
//
//    //간소화(Preview) 조회 by Name(부분 일치)
//    @GetMapping("/club/prev/{clubName}")
//    public Page<ClubPrevDTO> getClubPrevByName(@PathVariable String clubName, Pageable pageable) {
//        return null;
//    }
//
//    //간소화(Preview) 조회 by Campus
//    @GetMapping("/club/prev/{campus}")
//    public Page<ClubPrevDTO> getClubPrevByCampus(@PathVariable Campus campus, Pageable pageable) {
//        return null;
//    }
//
//    //간소화(Preview) 조회 by
//    @GetMapping("/club/prev/{activityType}")
//    public Page<ClubPrevDTO> getClubPrevByActivityType(@PathVariable String activityType, Pageable pageable) {
//        return null;
//    }


//=====UPDATE=====//

    //정보 변경
    @PatchMapping("/club/{clubId}")
    public ClubNameAndIdDTO updateClub(@PathVariable Long clubId, @ModelAttribute ClubCreateRequestDTO clubCreateRequestDTO) {
        return new ClubNameAndIdDTO();
    }

//=====DELETE=====//

    //특정 활동 사진 삭제
    @DeleteMapping("/club/{clubId}/{activityImageName}")
    public ActivityImageDeletionDTO deleteActivityImage(@PathVariable Long clubId, @PathVariable String activityImageName) {
        return null;
    }

    //삭제
    @DeleteMapping("/club/{clubId}")
    public ClubNameAndIdDTO deleteClubById(@PathVariable Long clubId) {
        return new ClubNameAndIdDTO();
    }

    //삭제 취소 (복구)
    @DeleteMapping("/club/{clubId}/cancel")
    public ClubNameAndIdDTO cancelClubDeletionById(@PathVariable Long clubId) {
        return new ClubNameAndIdDTO();
    }

}
