package com.skklub.admin.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.skklub.admin.domain.Recruit;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecruitDto {
    //모집 시기
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime recruitStartAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime recruitEndAt;
    //정원
    @NotBlank
    private String recruitQuota;
    //디테일
    @NotBlank
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

    public Recruit toEntity() {
        return new Recruit(recruitStartAt, recruitEndAt, recruitQuota, recruitProcessDescription, recruitContact, recruitWebLink);
    }
}
