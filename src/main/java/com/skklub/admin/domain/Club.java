package com.skklub.admin.domain;

import com.skklub.admin.domain.enums.Campus;
import com.skklub.admin.domain.enums.ClubType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    //Files
    @OneToOne(fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "logo")
    private Logo logo;
    @OneToMany(mappedBy = "club", orphanRemoval = true)
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
                String belongs,
                ClubType clubType,
                String briefActivityDescription,
                Campus campus,
                String clubDescription,
                Integer establishAt,
                String headLine,
                String mandatoryActivatePeriod,
                Integer memberAmount,
                String regularMeetingTime,
                String roomLocation,
                String webLink1,
                String webLink2
    ) {
        this.name = name;
        this.activityDescription = activityDescription;
        this.belongs = belongs;
        this.clubType = clubType;
        this.briefActivityDescription = briefActivityDescription;
        this.campus = campus;
        this.clubDescription = clubDescription;
        this.establishAt = establishAt;
        this.headLine = headLine;
        this.mandatoryActivatePeriod = mandatoryActivatePeriod;
        this.memberAmount = memberAmount;
        this.regularMeetingTime = regularMeetingTime;
        this.roomLocation = roomLocation;
        this.webLink1 = webLink1;
        this.webLink2 = webLink2;
    }

    public void update(Club updateInfo
    ) {
        this.name = updateInfo.name;
        this.activityDescription = updateInfo.activityDescription;
        this.belongs = updateInfo.belongs;
        this.clubType = updateInfo.clubType;
        this.briefActivityDescription = updateInfo.briefActivityDescription;
        this.campus = updateInfo.campus;
        this.clubDescription = updateInfo.clubDescription;
        this.establishAt = updateInfo.establishAt;
        this.headLine = updateInfo.headLine;
        this.mandatoryActivatePeriod = updateInfo.mandatoryActivatePeriod;
        this.memberAmount = updateInfo.memberAmount;
        this.regularMeetingTime = updateInfo.regularMeetingTime;
        this.roomLocation = updateInfo.roomLocation;
        this.webLink1 = updateInfo.webLink1;
        this.webLink2 = updateInfo.webLink2;
    }

    public String changeLogo(Logo logo) {
        String oldSavedName = Optional.ofNullable(this.logo).map(Logo::getUploadedName).orElse(null);
        this.logo = logo;
        return oldSavedName;
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
    public void endRecruit(){
        this.recruit = null;
    }


    public boolean remove() {
        if(alive) {
            alive = false;
            return true;
        }
        return false;
    }

    public boolean revive() {
        if (alive) {
            return false;
        }
        alive = true;
        return true;
    }

    //Must be Removed
    public void setUser(User user) {
        this.president = user;
    }

    public boolean onRecruit() {
        return recruit != null;
    }

    public boolean isAlive() {
        return alive;
    }
}
