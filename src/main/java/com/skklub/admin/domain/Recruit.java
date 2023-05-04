package com.skklub.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Getter
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
}
