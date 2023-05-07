package com.skklub.admin.controller;

import com.skklub.admin.controller.dto.*;
import com.skklub.admin.domain.Club;
import com.skklub.admin.domain.enums.Campus;
import com.skklub.admin.domain.enums.ClubType;
import com.skklub.admin.service.dto.ClubPrevDTO;
import com.skklub.admin.service.ClubService;
import com.skklub.admin.service.dto.ClubDetailInfoDto;
import com.skklub.admin.service.dto.FileNames;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ClubController {

    private final ClubService clubService;
    private final S3Transferer s3Transferer;

//=====CREATE=====//

    //추가
    @PostMapping(value = "/club")
    public ClubNameAndIdDTO createClub(@ModelAttribute ClubCreateRequestDTO clubCreateRequestDTO, @RequestParam MultipartFile logo) {
        log.info("club name : {}, logo size : {}", clubCreateRequestDTO.getClubName(), logo.getSize());
        FileNames uploadedLogo = s3Transferer.uploadOne(logo);
        Club club = clubCreateRequestDTO.toEntity();
        Long id = clubService.createClub(club, uploadedLogo.getOriginalName(), uploadedLogo.getSavedName());
        return new ClubNameAndIdDTO(id, club.getName());
    }

    //활동 사진 등록(LIST)
    @PostMapping("/club/{clubId}/activityImage")
    public ResponseEntity<ClubNameAndIdDTO> uploadActivityImages(@PathVariable Long clubId, @RequestParam List<MultipartFile> activityImages) {
        log.info("request CLUB : {}, file count : {}", clubId, activityImages.size());
        List<FileNames> savedActivityImages = s3Transferer.uploadAll(activityImages);
        return clubService.appendActivityImages(clubId, savedActivityImages)
                .map(name -> new ClubNameAndIdDTO(clubId, name))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.unprocessableEntity().build());
    }

//=====READ=====//

    //세부 정보 조회 by ID
    @GetMapping( "/club/{clubId}")
    public ResponseEntity<ClubResponseDTO> getClubById(@PathVariable Long clubId) {
        log.info("request CLUB Id : {}", clubId);
        return clubService.getClubDetailInfoById(clubId)
                .map(this::convertClubImagesToFile)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.unprocessableEntity().build());
    }

    //간소화(Preview) 조회
    @GetMapping("/club/prev/{campus}/{clubType}/{belongs}")
    public Page<ClubPrevResponseDTO> getClubPrevByCategories(@PathVariable Campus campus, @PathVariable Optional<ClubType> clubType, @PathVariable Optional<String> belongs, Pageable pageable) {
        Page<ClubPrevDTO> clubPrevs = clubService.getClubPrevsByCategories(campus, clubType, belongs, pageable);
        return convertClubPrevsLogoToFile(clubPrevs);
    }

    //이름 검색 완전 일치
    @GetMapping("/club/search/{name}")
    public ResponseEntity<ClubResponseDTO> getClubByName(@PathVariable String name) {
        return clubService.getClubDetailInfoByName(name)
                .map(this::convertClubImagesToFile)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    //이름 검색 부분 일치
    @GetMapping("/club/search/prev/{keyword}")
    public Page<ClubPrevResponseDTO> getClubPrevByKeyword(@PathVariable String keyword, Pageable pageable) {
        Page<ClubPrevDTO> clubPrevs = clubService.getClubPrevsByKeyword(keyword, pageable);
        return convertClubPrevsLogoToFile(clubPrevs);
    }

    //오늘의 추천 동아리
    @GetMapping("/club/random/{campus}/{clubType}/{belongs}")
    public List<ClubNameAndIdDTO> getRandomClubNameAndIdByCategories(@PathVariable Campus campus, @PathVariable Optional<ClubType> clubType, @PathVariable Optional<String> belongs) {
        return clubService.getRandomClubsByCategories(campus, clubType, belongs).stream()
                .map(dto -> new ClubNameAndIdDTO(dto.getId(), dto.getName()))
                .collect(Collectors.toList());
    }

    private Page<ClubPrevResponseDTO> convertClubPrevsLogoToFile(Page<ClubPrevDTO> clubPrevs) {
        Page<ClubPrevResponseDTO> response = clubPrevs.map(clubPrevDTO -> {
            FileNames logo = clubPrevDTO.getLogo();
            S3DownloadDto s3DownloadDto = s3Transferer.downloadOne(logo);
            return new ClubPrevResponseDTO(clubPrevDTO, s3DownloadDto);
        });
        return response;
    }

    private ClubResponseDTO convertClubImagesToFile(ClubDetailInfoDto dto) {
        S3DownloadDto logo = s3Transferer.downloadOne(dto.getLogo());
        List<S3DownloadDto> activityImages = s3Transferer.downloadAll(dto.getActivityImages());
        return new ClubResponseDTO(dto, logo, activityImages);
    }

//=====UPDATE=====//

    //정보 변경
    @PatchMapping("/club/{clubId}")
    public ResponseEntity<ClubNameAndIdDTO> updateClub(@PathVariable Long clubId, @ModelAttribute ClubCreateRequestDTO clubCreateRequestDTO) {
        return clubService.updateClub(clubId, clubCreateRequestDTO)
                .map(name -> new ClubNameAndIdDTO(clubId, name))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.unprocessableEntity().build());
    }

//=====DELETE=====//

    //특정 활동 사진 삭제
    @DeleteMapping("/club/{clubId}/{activityImageName}")
    public ResponseEntity<ActivityImageDeletionDTO> deleteActivityImage(@PathVariable Long clubId, @PathVariable String activityImageName) {
        return clubService.deleteActivityImage(clubId, activityImageName)
                .map(uploadedName -> {
                    s3Transferer.deleteOne(uploadedName);
                    return new ActivityImageDeletionDTO(clubId, activityImageName);
                })
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.unprocessableEntity().build());
    }

    //삭제
    @DeleteMapping("/club/{clubId}")
    public ResponseEntity<ClubNameAndIdDTO> deleteClubById(@PathVariable Long clubId) {
        return clubService.deleteClub(clubId)
                .map(name -> new ClubNameAndIdDTO(clubId, name))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.unprocessableEntity().build());
    }

    //삭제 취소 (복구)
    @DeleteMapping("/club/{clubId}/cancel")
    public ResponseEntity<ClubNameAndIdDTO> cancelClubDeletionById(@PathVariable Long clubId) {
        return clubService.reviveClub(clubId)
                .map(name -> new ClubNameAndIdDTO(clubId, name))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.unprocessableEntity().build());
    }

}
