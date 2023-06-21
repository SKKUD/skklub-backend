package com.skklub.admin.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Logo {
    @Id @Column(name = "logo_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String originalName;
    private String uploadedName;


    public Logo(String originalName, String uploadedName) {
        this.originalName = originalName;
        this.uploadedName = uploadedName;
    }

    public String update(String originalName, String savedName) {
        String old = this.uploadedName;
        this.originalName = originalName;
        this.uploadedName = savedName;
        return old;
    }
}
