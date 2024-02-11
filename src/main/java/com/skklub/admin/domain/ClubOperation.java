package com.skklub.admin.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

import java.util.Optional;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@Builder(builderMethodName = "hiddenBuilder")
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor
@Where(clause = "alive = true and visibility = true")
public class ClubOperation extends BaseEntity {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "club_operation_id")
    @Builder.Default
    private Long id = null;

    private String headLine;
    private String mandatoryActivatePeriod;
    private Integer memberAmount;
    private String regularMeetingTime;
    private String roomLocation;

    @Builder.Default
    private Boolean alive = true;
    @Builder.Default
    private Boolean visibility = true;

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

    public static ClubOperationBuilder builder(User president, ClubCategorization clubCategorization) {
        return hiddenBuilder()
                .id(null)
                .president(president)
                .clubCategorization(clubCategorization)
                .alive(true)
                .visibility(true);
    }
}
