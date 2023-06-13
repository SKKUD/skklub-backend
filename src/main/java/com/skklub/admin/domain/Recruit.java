package com.skklub.admin.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Getter
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
        this.startAt = startAt.truncatedTo(ChronoUnit.SECONDS);
        this.endAt = endAt.truncatedTo(ChronoUnit.SECONDS);
        this.quota = quota;
        this.processDescription = processDescription;
        this.contact = contact;
        this.webLink = webLink;
    }
}
