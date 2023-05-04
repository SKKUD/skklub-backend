package com.skklub.admin.domain;

import com.skklub.admin.domain.enums.ActivityType;
import com.skklub.admin.domain.enums.Campus;
import com.skklub.admin.domain.enums.ClubType;
import com.skklub.admin.domain.enums.College;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Club extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "club_id")
    private Long id;

    private Boolean alive; //Y N OX// Dead Alive

    //분류
    @Enumerated(EnumType.STRING)
    private Campus campus;
    @Enumerated(EnumType.STRING)
    private ClubType clubType;
    @Enumerated(EnumType.STRING)
    private College college;
    @Enumerated(EnumType.STRING)
    private ActivityType activityType;
    private String briefActivityDescription;

    //Outlines
    private String name;
    private String headLine;
    private String establishAt;
    private String roomLocation;
    private Integer memberAmount;
    private String regularMeetingTime;
    private String mandatoryActivatePeriod;

    //Details
    @Lob
    private String clubDescription;
    @Lob
    private String activityDescription;

    //Files
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "logo")
    private Logo logo;
    @OneToMany(mappedBy = "club")
    private List<ActivityImage> activityImages = new ArrayList<>();
    private String webLink1;
    private String webLink2;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User president;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruit_id")
    private Recruit recruit;


    public Club(String name,
                String activityDescription,
                ActivityType activityType,
                ClubType clubType,
                String briefActivityDescription,
                Campus campus,
                String clubDescription,
                College college,
                String establishAt,
                String headLine,
                String mandatoryActivatePeriod,
                Integer memberAmount,
                String regularMeetingTime,
                String roomLocation,
                String webLink1,
                String webLink2) {
        this.name = name;
        this.activityDescription = activityDescription;
        this.activityType = activityType;
        this.clubType = clubType;
        this.briefActivityDescription = briefActivityDescription;
        this.campus = campus;
        this.clubDescription = clubDescription;
        this.college = college;
        this.establishAt = establishAt;
        this.headLine = headLine;
        this.mandatoryActivatePeriod = mandatoryActivatePeriod;
        this.memberAmount = memberAmount;
        this.regularMeetingTime = regularMeetingTime;
        this.roomLocation = roomLocation;
        this.webLink1 = webLink1;
        this.webLink2 = webLink2;
    }

    //Must be Removed
    public void matchLogo(Logo logo) {
        this.logo = logo;
    }

    public void appendActivityImages(List<ActivityImage> activityImages) {
        for (ActivityImage activityImage : activityImages) {
            this.activityImages.add(activityImage);
            activityImage.setClub(this);
        }
    }

    public void startRecruit(Recruit recruit) {
        this.recruit = recruit;
    }

    //Must be Removed
    public void setUser(User user) {
        this.president = user;
    }
}
