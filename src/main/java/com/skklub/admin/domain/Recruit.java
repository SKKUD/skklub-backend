package com.skklub.admin.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Getter
@EqualsAndHashCode(exclude = {"id"}, callSuper = false)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Recruit extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recruit_id")
    private Long id;

    //모집 시기
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    //정원
    private String quota;
    //디테일
    private String processDescription;
    //모집관련 연락처
    private String contact;
    private String webLink;

    public Recruit(LocalDateTime startAt, LocalDateTime endAt, String quota, String processDescription, String contact, String webLink) {
        this.startAt = startAt;
        this.endAt = endAt;
        this.quota = quota;
        this.processDescription = processDescription;
        this.contact = contact;
        this.webLink = webLink;
    }

    public void update(Recruit recruit) {
        this.startAt = recruit.getStartAt();
        this.endAt = recruit.getEndAt();
        this.quota = recruit.getQuota();
        this.processDescription = recruit.getProcessDescription();
        this.contact = recruit.getContact();
        this.webLink = recruit.getWebLink();
    }
}
