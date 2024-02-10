package com.skklub.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
@Inheritance(strategy = InheritanceType.JOINED)
public class FileName extends BaseTimeEntity{
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "file_name_id")
    private Long id;

    private String originalName;
    private String uploadedName;

    public FileName(String originalName, String uploadedName) {
        this.originalName = originalName;
        this.uploadedName = uploadedName;
    }
}
