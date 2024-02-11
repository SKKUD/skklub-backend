package com.skklub.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

import java.util.Optional;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
@Where(clause = "alive = true and visibility = true")
public class ClubOperation extends BaseEntity {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "club_operation_id")
    private Long id;

    private String headLine;
    private String mandatoryActivatePeriod;
    private Integer memberAmount;
    private String regularMeetingTime;
    private String roomLocation;

    private Boolean alive;
    private Boolean visibility;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User president;
    @OneToOne
    @JoinColumn(name = "club_categorization_id")
    private ClubCategorization clubCategorization;

    public Optional<String> getHeadLine() {
        return Optional.ofNullable(headLine);
    }

    public Optional<String> getMandatoryActivatePeriod() {
        return Optional.ofNullable(mandatoryActivatePeriod);
    }

    public Optional<Integer> getMemberAmount() {
        return Optional.ofNullable(memberAmount);
    }

    public Optional<String> getRegularMeetingTime() {
        return Optional.ofNullable(regularMeetingTime);
    }

    public Optional<String> getRoomLocation() {
        return Optional.ofNullable(roomLocation);
    }


}
