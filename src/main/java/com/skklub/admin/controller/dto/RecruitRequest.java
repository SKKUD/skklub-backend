package com.skklub.admin.controller.dto;

import com.skklub.admin.domain.Recruit;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class RecruitRequest {
    //모집 시기
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime recruitStartAt;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
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

    public Recruit toEntity() {
        return new Recruit(recruitStartAt, recruitEndAt, recruitQuota, recruitProcessDescription, recruitContact, recruitWebLink);
    }
}
