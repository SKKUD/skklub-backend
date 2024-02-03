package com.skklub.admin.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@DiscriminatorColumn
@Inheritance(strategy = InheritanceType.JOINED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FileName extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_name_id")
    private Long id;

    private String originalName;
    private String uploadedName;

    public FileName(String originalName, String uploadedName) {
        this.originalName = originalName;
        this.uploadedName = uploadedName;
    }

    public void update(FileName logo) {
        this.originalName = logo.getOriginalName();
        this.uploadedName = logo.getUploadedName();
    }

}
