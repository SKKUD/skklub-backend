package com.skklub.admin.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@Builder(builderMethodName = "hiddenBuilder")
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor
public class ClubMeta extends BaseEntity {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "club_meta_id")
    @Builder.Default
    private Long id = null;

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

    public static ClubMetaBuilder builder(String name, String activityType, String description, String activityDescription) {
        return hiddenBuilder()
                .id(null)
                .name(name)
                .activityType(activityType)
                .description(description)
                .activityDescription(activityDescription);
    }
}
