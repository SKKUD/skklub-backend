package com.skklub.admin.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ActivityImage {
    @Id @Column(name = "activity_image_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    private Club club;

    private String originalName;
    private String uploadedName;

    public ActivityImage(String originalName, String uploadedName) {
        this.originalName = originalName;
        this.uploadedName = uploadedName;
    }

    public void setClub(Club club) {
        this.club = club;
    }
}
