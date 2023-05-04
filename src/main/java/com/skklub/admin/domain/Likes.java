package com.skklub.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class Likes extends BaseTimeEntity {
    @Id @Column(name = "likes_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    private Club club;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
