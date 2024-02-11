package com.skklub.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class ClubMeta extends BaseEntity {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "club_meta_id")
    private Long id;

    private String name;
    private String activityType;

    @Lob
    private String description;
    @Lob
    private String activityDescription;

    private Integer establishAt;

    @OneToOne
    @JoinColumn(name = "logo_id")
    private Logo logo;

    public Optional<Integer> getEstablishAt() {
        return Optional.ofNullable(establishAt);
    }

    public Optional<Logo> getLogo() {
        return Optional.ofNullable(logo);
    }

    /**
     * 생성자 Without 설립 연도
     */
    public ClubMeta(String name, String activityType, String description, String activityDescription) {
        this.name = name;
        this.activityType = activityType;
        this.description = description;
        this.activityDescription = activityDescription;
    }

    /**
     * 생성자 With 설립 연도
     */
    public ClubMeta(String name, String activityType, String description, String activityDescription, Integer establishAt) {
        this.name = name;
        this.activityType = activityType;
        this.description = description;
        this.activityDescription = activityDescription;
        this.establishAt = establishAt;
    }
}
