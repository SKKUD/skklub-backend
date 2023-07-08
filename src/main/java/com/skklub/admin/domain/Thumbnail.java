package com.skklub.admin.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Thumbnail {
    @Id
    @GeneratedValue
    @Column(name = "thumnail_id")
    private Long id;

    private String originalName;
    private String uploadedName;

    public Thumbnail(String originalName, String uploadedName) {
        this.originalName = originalName;
        this.uploadedName = uploadedName;
    }

    public void update(Thumbnail logo) {
        this.originalName = logo.getOriginalName();
        this.uploadedName = logo.getUploadedName();
    }
}
