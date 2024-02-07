package com.skklub.admin.controller;

import com.skklub.admin.controller.dto.*;
import com.skklub.admin.domain.ActivityImage;
import com.skklub.admin.domain.Logo;
import com.skklub.admin.domain.User;
import com.skklub.admin.domain.enums.Role;
import com.skklub.admin.exception.deprecated.error.exception.*;
import com.skklub.admin.exception.deprecated.error.handler.ClubValidator;
import com.skklub.admin.domain.Club;
import com.skklub.admin.domain.enums.Campus;
import com.skklub.admin.domain.enums.ClubType;
import com.skklub.admin.repository.ClubRepository;
import com.skklub.admin.repository.UserRepository;
import com.skklub.admin.security.jwt.TokenProvider;
import com.skklub.admin.service.ClubService;
import com.skklub.admin.service.dto.ClubDetailInfoDto;
import com.skklub.admin.service.dto.FileNames;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
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
    private final UserRepository userRepository;
    private final S3Transferer s3Transferer;
    private final AuthValidator authValidator;
    private final static String DEFAULT_LOGO_NAME = "alt.jpg";

//=====CREATE=====//

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
    @GetMapping("/club/my")
    public ResponseEntity<ClubResponseDTO> getMyClubByLoginUser(@AuthenticationPrincipal UserDetails userDetails) {
        String username = TokenProvider.getAuthentication(userDetails).getName();
        User user = userRepository.findByUsername(username);
        if(!user.getRole().equals(Role.ROLE_USER)) throw new AdminCannotHaveClubException();
        try {
            Optional<Club> club = clubRepository.findDetailClubByPresident(user);
            return club.map(ClubDetailInfoDto::new)
                    .map(this::convertClubImagesToFile)
                    .map(ResponseEntity::ok)
                    .orElseThrow(UserWithNoClubException::new);
        } catch (IncorrectResultSizeDataAccessException e) {
            log.info("e : {}", e);
            e.printStackTrace();
            log.error("user : {}, 한 명의 유저에 여러개의 클럽이 매핑 돼 있습니다", user.getUsername());
            return ResponseEntity.internalServerError().build();
        } catch (UserWithNoClubException e) {
            log.error("user : {}, 이 유저는 동아리와 매핑되지 않은 유저 입니다", user.getUsername());
            return ResponseEntity.internalServerError().build();
        }
    }

    //간소화(Preview) 조회
    @GetMapping("/club/prev")
    public Page<ClubPrevResponseDTO> getClubPrevByCategories(@RequestParam Campus campus,
                                                             @RequestParam(required = false, defaultValue = "전체") ClubType clubType,
                                                             @RequestParam(required = false, defaultValue = "전체") String belongs,
                                                             Pageable pageable) {
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort().and(Sort.by("name").ascending()));
        ClubValidator.validateBelongs(campus, clubType, belongs);
        Page<Club> clubs = clubService.getClubPrevsByCategories(campus, clubType, belongs, pageRequest);
        return convertClubsLogoToFile(clubs);
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
        if(!StringUtils.hasText(keyword)) return Page.empty();
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort().and(Sort.by("name").ascending()));
        Page<Club> clubs = clubRepository.findClubByNameContaining(keyword, pageRequest);
        return convertClubsLogoToFile(clubs);
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

    private Page<ClubPrevResponseDTO> convertClubsLogoToFile(Page<Club> clubs) {
        Page<ClubPrevResponseDTO> response = clubs.map(club -> getClubPrevResponseDTO(club));
        return response;
    }

    @Async
    public ClubPrevResponseDTO getClubPrevResponseDTO(Club club) {
        FileNames logo = new FileNames(club.getLogo());
        S3DownloadDto s3DownloadDto = s3Transferer.downloadOne(logo);
        return new ClubPrevResponseDTO(club, s3DownloadDto);
    }

    private ClubResponseDTO convertClubImagesToFile(ClubDetailInfoDto dto) {
        S3DownloadDto logo = s3Transferer.downloadOne(dto.getLogo());
        List<S3DownloadDto> activityImages = s3Transferer.downloadAll(dto.getActivityImages());
        return new ClubResponseDTO(dto, logo, activityImages);
    }

//=====UPDATE=====//

    //정보 변경
    @PatchMapping("/club/{clubId}")
    public ResponseEntity<ClubNameAndIdDTO> updateClub(@PathVariable Long clubId, @ModelAttribute ClubUpdateRequest clubUpdateRequest) {
        authValidator.validateUpdatingClub(clubId);
        Club club = clubUpdateRequest.toEntity();
        return clubService.updateClub(clubId, club)
                .map(name -> new ClubNameAndIdDTO(clubId, name))
                .map(ResponseEntity::ok)
                .orElseThrow(ClubIdMisMatchException::new);
    }

    //로고 변경
    @PostMapping("/club/{clubId}/logo")
    public ResponseEntity<ClubIdAndLogoNameDTO> updateLogo(@PathVariable Long clubId, @RequestParam MultipartFile logo) {
        authValidator.validateUpdatingClub(clubId);
        Logo logoUpdateInfo = s3Transferer.uploadOne(logo).toLogoEntity();
        return clubService.updateLogo(clubId, logoUpdateInfo)
                .map(oldLogoName -> {
                    if(!oldLogoName.equals(DEFAULT_LOGO_NAME)) s3Transferer.deleteOne(oldLogoName);
                    return new ClubIdAndLogoNameDTO(clubId, logoUpdateInfo);
                })
                .map(ResponseEntity::ok)
                .orElseThrow(ClubIdMisMatchException::new);
    }

    //중동 -> 준중동
    @PatchMapping("/club/{clubId}/down")
    public ClubIdAndCategoryResponse downGradeClub(@PathVariable Long clubId){
        authValidator.validateUpdatingClub(clubId);
        Club clubAfterUpdate = clubService.downGrade(clubId).orElseThrow(ClubIdMisMatchException::new);
        return new ClubIdAndCategoryResponse(clubAfterUpdate);
    }

    //준중동 -> 중동
    @PatchMapping("/club/{clubId}/up")
    public ClubIdAndCategoryResponse upGradeClub(@PathVariable Long clubId){
        authValidator.validateUpdatingClub(clubId);
        Club clubAfterUpdate = clubService.upGrade(clubId).orElseThrow(ClubIdMisMatchException::new);
        return new ClubIdAndCategoryResponse(clubAfterUpdate);
    }

//====DELETE=====//

    //특정 활동 사진 삭제
    @DeleteMapping("/club/{clubId}/activityImage")
    public ResponseEntity<ActivityImageDeletionDTO> deleteActivityImage(@PathVariable Long clubId, @RequestParam String activityImageName) {
        authValidator.validateUpdatingClub(clubId);
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
        authValidator.validateUpdatingClub(clubId);
        return clubService.deleteClub(clubId)
                .map(name -> new ClubNameAndIdDTO(clubId, name))
                .map(ResponseEntity::ok)
                .orElseThrow(MissingAliveClubException::new);
    }

    //삭제 취소 (복구)
    @DeleteMapping("/club/{clubId}/cancel")
    public ResponseEntity<ClubNameAndIdDTO> cancelClubDeletionById(@PathVariable Long clubId) {
        authValidator.validateDeletionAuth(clubId);
        return clubService.reviveClub(clubId)
                .map(name -> new ClubNameAndIdDTO(clubId, name))
                .map(ResponseEntity::ok)
                .orElseThrow(MissingDeletedClubException::new);
    }


//====DEPRECATED=====//

    //추가
//    @PostMapping(value = "/club")
    public ClubNameAndIdDTO createClub(@ModelAttribute @Valid ClubCreateRequestDTO clubCreateRequestDTO, @RequestParam(required = false) MultipartFile logo) {
        ClubValidator.validateBelongs(clubCreateRequestDTO.getCampus(), clubCreateRequestDTO.getClubType(), clubCreateRequestDTO.getBelongs());
        FileNames uploadedLogo = Optional.ofNullable(logo)
                .map(s3Transferer::uploadOne)
                .orElse(new FileNames(DEFAULT_LOGO_NAME, DEFAULT_LOGO_NAME));
        Club club = clubCreateRequestDTO.toEntity();
        Logo logoAfterUpload = uploadedLogo.toLogoEntity();
        Long id = clubService.createClub(club, logoAfterUpload);
        return new ClubNameAndIdDTO(id, club.getName());
    }

}
