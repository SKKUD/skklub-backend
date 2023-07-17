package com.skklub.admin.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Thumbnail extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "thumbnail_id")
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
