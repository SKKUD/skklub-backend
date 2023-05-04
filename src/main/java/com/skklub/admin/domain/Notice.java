package com.skklub.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class Notice {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_id")
    private Long id;

    private String title;
    private String content;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User writer;

    private String thumbnailSrc;
}
