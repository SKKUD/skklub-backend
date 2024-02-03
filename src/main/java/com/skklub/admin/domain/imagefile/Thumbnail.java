package com.skklub.admin.domain.imagefile;

import com.skklub.admin.domain.FileName;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Thumbnail extends FileName {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "thumbnail_id")
    private Long id;

    public Thumbnail(String originalName, String uploadedName) {
        super(originalName, uploadedName);
    }

    public void update(Thumbnail logo) {
        super.update(logo);
    }
}
