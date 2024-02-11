package com.skklub.admin.domain;

import com.skklub.admin.domain.enums.Campus;
import com.skklub.admin.domain.enums.ClubType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class ClubCategorization {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "club_categorization_id")
    private Long id = null;

    @Enumerated(EnumType.STRING)
    private Campus campus;
    @Enumerated(EnumType.STRING)
    private ClubType clubType;
    private String belongs;

    public ClubCategorization(Campus campus, ClubType clubType, String belongs) {
        this.campus = campus;
        this.clubType = clubType;
        this.belongs = belongs;
    }

}
