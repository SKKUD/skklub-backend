package com.skklub.admin.controller;

import com.skklub.admin.controller.dto.ClubNameAndIdDTO;
import com.skklub.admin.controller.dto.RecruitRequest;
import com.skklub.admin.exception.deprecated.error.exception.ClubIdMisMatchException;
import com.skklub.admin.exception.deprecated.error.handler.ClubValidator;
import com.skklub.admin.domain.Recruit;
import com.skklub.admin.service.RecruitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class RecruitController {

    private final RecruitService recruitService;
    private final AuthValidator authValidator;

    //모집 등록
    @PostMapping("/recruit/{clubId}")
    public ResponseEntity<ClubNameAndIdDTO> startRecruit(@PathVariable Long clubId, @ModelAttribute @Valid RecruitRequest recruitRequest) {
        authValidator.validateUpdatingClub(clubId);
        ClubValidator.validateRecruitTimeFormat(recruitRequest);
        Recruit recruit = recruitRequest.toEntity();
        return recruitService.startRecruit(clubId, recruit)
                .map(name -> new ClubNameAndIdDTO(clubId, name))
                .map(ResponseEntity::ok)
                .orElseThrow(ClubIdMisMatchException::new);
    }

    //모집 수정
    @PatchMapping("/recruit/{clubId}")
    public ResponseEntity<Long> updateRecruit(@PathVariable Long clubId, @ModelAttribute @Valid RecruitRequest recruitRequest) {
        authValidator.validateUpdatingClub(clubId);
        ClubValidator.validateRecruitTimeFormat(recruitRequest);
        Recruit recruit = recruitRequest.toEntity();
        return recruitService.updateRecruit(clubId, recruit)
                .map(ResponseEntity::ok)
                .orElseThrow(ClubIdMisMatchException::new);
    }

    //모집 종료
    @DeleteMapping("/recruit/{clubId}")
    public Long endRecruit(@PathVariable Long clubId) {
        authValidator.validateUpdatingClub(clubId);
        recruitService.endRecruit(clubId);
        return clubId;
    }
}
