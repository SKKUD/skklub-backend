package com.skklub.admin.domain.imagefile;

import com.skklub.admin.domain.FileName;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Logo extends FileName {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "logo_id")
    private Long id;

    public Logo(String originalName, String uploadedName) {
        super(originalName, uploadedName);
    }

    public void update(Logo logo) {
        super.update(logo);
    }
}
