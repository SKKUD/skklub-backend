package com.skklub.admin.controller;

import com.skklub.admin.controller.dto.*;
import com.skklub.admin.domain.ActivityImage;
import com.skklub.admin.domain.Logo;
import com.skklub.admin.error.exception.ActivityImageMisMatchException;
import com.skklub.admin.error.exception.ClubIdMisMatchException;
import com.skklub.admin.error.exception.ClubNameMisMatchException;
import com.skklub.admin.error.handler.ClubValidator;
import com.skklub.admin.domain.Club;
import com.skklub.admin.domain.enums.Campus;
import com.skklub.admin.domain.enums.ClubType;
import com.skklub.admin.repository.ClubRepository;
import com.skklub.admin.service.dto.ClubPrevDTO;
import com.skklub.admin.service.ClubService;
import com.skklub.admin.service.dto.ClubDetailInfoDto;
import com.skklub.admin.service.dto.FileNames;
import jakarta.validation.Valid;
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
    private final ClubRepository clubRepository;
    private final S3Transferer s3Transferer;
    private final static String DEFAULT_LOGO_NAME = "alt.jpg";

//=====CREATE=====//

    //추가
    @PostMapping(value = "/club")
    public ClubNameAndIdDTO createClub(@ModelAttribute @Valid ClubCreateRequestDTO clubCreateRequestDTO, @RequestParam(required = false) MultipartFile logo) {
        log.info("clubCreateRequestDTO.getClubType() : {}", clubCreateRequestDTO.getClubType());
        ClubValidator.validateBelongs(clubCreateRequestDTO.getCampus(), clubCreateRequestDTO.getClubType(), clubCreateRequestDTO.getBelongs());
        FileNames uploadedLogo = Optional.ofNullable(logo).map(s3Transferer::uploadOne).orElse(new FileNames(DEFAULT_LOGO_NAME, DEFAULT_LOGO_NAME));
        Club club = clubCreateRequestDTO.toEntity();
        Logo logoAfterUpload = uploadedLogo.toLogoEntity();
        Long id = clubService.createClub(club, logoAfterUpload);
        return new ClubNameAndIdDTO(id, club.getName());
    }

    //활동 사진 등록(LIST)
    @PostMapping("/club/{clubId}/activityImage")
    public ResponseEntity<ClubNameAndIdDTO> uploadActivityImages(@PathVariable Long clubId, @RequestParam List<MultipartFile> activityImages) {
        List<ActivityImage> savedActivityImages = s3Transferer.uploadAll(activityImages).stream()
                .map(FileNames::toActivityImageEntity)
                .collect(Collectors.toList());
        return clubService.appendActivityImages(clubId, savedActivityImages)
                .map(name -> new ClubNameAndIdDTO(clubId, name))
                .map(ResponseEntity::ok)
                .orElseThrow(ClubIdMisMatchException::new);
    }

//=====READ=====//

    //세부 정보 조회 by ID
    @GetMapping("/club/{clubId}")
    public ResponseEntity<ClubResponseDTO> getClubById(@PathVariable Long clubId) {
        return clubRepository.findDetailClubById(clubId)
                .map(ClubDetailInfoDto::new)
                .map(this::convertClubImagesToFile)
                .map(ResponseEntity::ok)
                .orElseThrow(ClubIdMisMatchException::new);
    }

    //간소화(Preview) 조회
    @GetMapping("/club/prev")
    public Page<ClubPrevResponseDTO> getClubPrevByCategories(@RequestParam Campus campus,
                                                             @RequestParam(required = false, defaultValue = "전체") ClubType clubType,
                                                             @RequestParam(required = false, defaultValue = "전체") String belongs,
                                                             Pageable pageable) {
        ClubValidator.validateBelongs(campus, clubType, belongs);
        Page<ClubPrevDTO> clubPrevs = clubService.getClubPrevsByCategories(campus, clubType, belongs, pageable)
                .map(ClubPrevDTO::fromEntity);
        return convertClubPrevsLogoToFile(clubPrevs);
    }

    //이름 검색 완전 일치
    @GetMapping("/club/search")
    public ResponseEntity<ClubResponseDTO> getClubByName(@RequestParam String name) {
        return clubRepository.findDetailClubByName(name)
                .map(ClubDetailInfoDto::new)
                .map(this::convertClubImagesToFile)
                .map(ResponseEntity::ok)
                .orElseThrow(ClubNameMisMatchException::new);
    }

    //이름 검색 부분 일치
    @GetMapping("/club/search/prevs")
    public Page<ClubPrevResponseDTO> getClubPrevByKeyword(@RequestParam String keyword, Pageable pageable) {
        Page<ClubPrevDTO> clubPrevs = clubRepository.findClubByNameContainingOrderByName(keyword, pageable)
                .map(ClubPrevDTO::fromEntity);
        return convertClubPrevsLogoToFile(clubPrevs);
    }

    //오늘의 추천 동아리
    @GetMapping("/club/random")
    public List<RandomClubsResponse> getRandomClubNameAndIdByCategories(@RequestParam Campus campus,
                                                                     @RequestParam(required = false, defaultValue = "전체") ClubType clubType,
                                                                     @RequestParam(required = false, defaultValue = "전체") String belongs) {
        ClubValidator.validateBelongs(campus, clubType, belongs);
        return clubService.getRandomClubsByCategories(campus, clubType, belongs).stream()
                .map(RandomClubsResponse::new)
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
        log.info("logo: {}", logo.getFileName());
        List<S3DownloadDto> activityImages = s3Transferer.downloadAll(dto.getActivityImages());
        return new ClubResponseDTO(dto, logo, activityImages);
    }

//=====UPDATE=====//

    //정보 변경
    @PatchMapping("/club/{clubId}")
    public ResponseEntity<ClubNameAndIdDTO> updateClub(@PathVariable Long clubId, @ModelAttribute ClubCreateRequestDTO clubCreateRequestDTO) {
        ClubValidator.validateBelongs(clubCreateRequestDTO.getCampus(), clubCreateRequestDTO.getClubType(), clubCreateRequestDTO.getBelongs());
        Club club = clubCreateRequestDTO.toEntity();
        return clubService.updateClub(clubId, club)
                .map(name -> new ClubNameAndIdDTO(clubId, name))
                .map(ResponseEntity::ok)
                .orElseThrow(ClubIdMisMatchException::new);
    }

    //로고 변경
    @PostMapping("/club/{clubId}/logo")
    public ResponseEntity<ClubIdAndLogoNameDTO> updateLogo(@PathVariable Long clubId, @RequestParam MultipartFile logo) {
        Logo logoUpdateInfo = s3Transferer.uploadOne(logo).toLogoEntity();
        return clubService.updateLogo(clubId, logoUpdateInfo)
                .map(oldLogoName -> {
                    if(!oldLogoName.equals(DEFAULT_LOGO_NAME)) s3Transferer.deleteOne(oldLogoName);
                    return new ClubIdAndLogoNameDTO(clubId, logoUpdateInfo);
                })
                .map(ResponseEntity::ok)
                .orElseThrow(ClubIdMisMatchException::new);
    }

//====DELETE=====//

    //특정 활동 사진 삭제
    @DeleteMapping("/club/{clubId}/activityImage")
    public ResponseEntity<ActivityImageDeletionDTO> deleteActivityImage(@PathVariable Long clubId, @RequestParam String activityImageName) {
        return clubService.deleteActivityImage(clubId, activityImageName)
                .map(uploadedName -> {
                    s3Transferer.deleteOne(uploadedName);
                    return new ActivityImageDeletionDTO(clubId, activityImageName);
                })
                .map(ResponseEntity::ok)
                .orElseThrow(ActivityImageMisMatchException::new);
    }

    //삭제
    @DeleteMapping("/club/{clubId}")
    public ResponseEntity<ClubNameAndIdDTO> deleteClubById(@PathVariable Long clubId) {
        return clubService.deleteClub(clubId)
                .map(name -> new ClubNameAndIdDTO(clubId, name))
                .map(ResponseEntity::ok)
                .orElseThrow(ClubIdMisMatchException::new);
    }

    //삭제 취소 (복구)
    @DeleteMapping("/club/{clubId}/cancel")
    public ResponseEntity<ClubNameAndIdDTO> cancelClubDeletionById(@PathVariable Long clubId) {
        return clubService.reviveClub(clubId)
                .map(name -> new ClubNameAndIdDTO(clubId, name))
                .map(ResponseEntity::ok)
                .orElseThrow(ClubIdMisMatchException::new);
    }

}
