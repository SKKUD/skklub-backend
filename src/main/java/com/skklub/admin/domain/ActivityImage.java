package com.skklub.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class ActivityImage {
    @Id @Column(name = "activity_image_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    private Club club;

    private String src;
    private String name;
}
