package com.skklub.admin.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.skklub.admin.domain.Recruit;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

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
    @NotNull
    private String recruitQuota;
    //디테일
    @NotNull
    private String recruitProcessDescription;
    //모집관련 연락처
    private String recruitContact;
    private String recruitWebLink;

    public RecruitDto(Recruit recruit) {
        if(recruit.getEndAt() != null && recruit.getStartAt() != null) {
            this.recruitStartAt = recruit.getStartAt().truncatedTo(ChronoUnit.MINUTES);
            this.recruitEndAt = recruit.getEndAt().truncatedTo(ChronoUnit.MINUTES);
        }
        this.recruitQuota = recruit.getQuota();
        this.recruitProcessDescription = recruit.getProcessDescription();
        this.recruitContact = recruit.getContact();
        this.recruitWebLink = recruit.getWebLink();
    }

    public Recruit toEntity() {
        if(this.recruitStartAt != null && this.recruitEndAt != null) {
            this.recruitStartAt = this.recruitStartAt.truncatedTo(ChronoUnit.MINUTES);
            this.recruitEndAt = this.recruitEndAt.truncatedTo(ChronoUnit.MINUTES);
        }
        return new Recruit(recruitStartAt, recruitEndAt, recruitQuota, recruitProcessDescription, recruitContact, recruitWebLink);
    }
}
