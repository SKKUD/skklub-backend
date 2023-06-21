package com.skklub.admin.controller;

import com.skklub.admin.controller.dto.ClubNameAndIdDTO;
import com.skklub.admin.controller.dto.RecruitDto;
import com.skklub.admin.error.exception.ClubIdMisMatchException;
import com.skklub.admin.error.exception.RecruitIdMisMatchException;
import com.skklub.admin.error.handler.ClubValidator;
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

    //모집 등록
    @PostMapping("/recruit/{clubId}")
    public ResponseEntity<ClubNameAndIdDTO> startRecruit(@PathVariable Long clubId, @RequestBody @Valid RecruitDto recruitDto) {
        ClubValidator.validateRecruitTimeFormat(recruitDto);
        Recruit recruit = recruitDto.toEntity();
        return recruitService.startRecruit(clubId, recruit)
                .map(name -> new ClubNameAndIdDTO(clubId, name))
                .map(ResponseEntity::ok)
                .orElseThrow(ClubIdMisMatchException::new);
    }

    //모집 수정
    @PatchMapping("/recruit/{recruitId}")
    public ResponseEntity<Long> updateRecruit(@PathVariable Long recruitId, @RequestBody @Valid RecruitDto recruitDto) {
        ClubValidator.validateRecruitTimeFormat(recruitDto);
        Recruit recruit = recruitDto.toEntity();
        return recruitService.updateRecruit(recruitId, recruit)
                .map(ResponseEntity::ok)
                .orElseThrow(RecruitIdMisMatchException::new);
    }

    //모집 종료
    @DeleteMapping("/recruit/{recruitId}")
    public Long endRecruit(@PathVariable Long recruitId) {
        recruitService.endRecruit(recruitId);
        return recruitId;
    }
}
