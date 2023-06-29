package com.skklub.admin.domain;

import com.skklub.admin.domain.enums.Campus;
import com.skklub.admin.domain.enums.ClubType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeletedClub extends BaseEntity{
    @Id
    @Column(name = "club_id")
    private Long id;

    //분류
    @Enumerated(EnumType.STRING)
    private Campus campus;
    @Enumerated(EnumType.STRING)
    private ClubType clubType;
    private String belongs;
    private String briefActivityDescription;

    //Outlines
    private String name;
    private String headLine;
    private Integer establishAt;
    private String roomLocation;
    private Integer memberAmount;
    private String regularMeetingTime;
    private String mandatoryActivatePeriod;

    //Details
    @Lob
    private String clubDescription;
    @Lob
    private String activityDescription;

    private Long logoId;

    private String webLink1;
    private String webLink2;

    private Long userId;

    private Long recruitId;

}
