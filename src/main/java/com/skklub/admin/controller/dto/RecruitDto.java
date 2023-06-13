package com.skklub.admin.controller.dto;

import com.skklub.admin.domain.Recruit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Optional;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecruitDto {
    //모집 시기
    private LocalDateTime recruitStartAt;
    private LocalDateTime recruitEndAt;
    //정원
    private String recruitQuota;
    //디테일
    private String recruitProcessDescription;
    //모집관련 연락처
    private String recruitContact;
    private String recruitWebLink;

    public RecruitDto(Recruit recruit) {
        this.recruitStartAt = recruit.getStartAt();
        this.recruitEndAt = recruit.getEndAt();
        this.recruitQuota = recruit.getQuota();
        this.recruitProcessDescription = recruit.getProcessDescription();
        this.recruitContact = recruit.getContact();
        this.recruitWebLink = recruit.getWebLink();
    }
}
