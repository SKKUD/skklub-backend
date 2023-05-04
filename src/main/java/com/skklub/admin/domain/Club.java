package com.skklub.admin.domain;

import com.skklub.admin.domain.enums.ActivityType;
import com.skklub.admin.domain.enums.Campus;
import com.skklub.admin.domain.enums.ClubType;
import com.skklub.admin.domain.enums.College;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
public class Club extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "club_id")
    private Long id;

    private String name;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User president;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruit_id")
    private Recruit recruit;

    @Enumerated(EnumType.STRING)
    private ClubType clubType;

    @Enumerated(EnumType.STRING)
    private Campus campus;

    //    @ColumnDefault("전체")
    @Enumerated(EnumType.STRING)
    private College college;
    @Enumerated(EnumType.STRING)
    private ActivityType activityType;

    private String briefActivityDescription;

    private String roomLocation;
    private Integer memberAmount;
    private String regularMeetingTime;

    private LocalDate establishAt;

    private String logoSrc;
    @OneToMany(mappedBy = "club")
    private List<ActivityImage> activityImages;

    //Up to 2=================
    private String webLink1;
    private String webLink2;
    //========================

    private String headLine;
    private String clubDescription;
    private String activityDescription;
    //의무활동 기간
    private String mandatoryActivatePeriod;

    private Boolean alive;

}
