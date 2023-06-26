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
public class DeletedClub{
    @Id
    @Column(name = "deleted_club_id")
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
    @OneToOne(fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "logo")
    private Logo logo;
    @OneToMany(mappedBy = "club", orphanRemoval = true)
    private List<ActivityImage> activityImages = new ArrayList<>();
    private String webLink1;
    private String webLink2;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User president;

    //No Recruit Information!!!


    public boolean revive() {
        if (alive) {
            return false;
        }
        alive = true;
        return true;
    }

    public boolean isAlive() {
        return alive;
    }

    public DeletedClub(Club club) {
        this.id = club.getId();
        this.alive = club.getAlive();
        this.campus = club.getCampus();
        this.clubType = club.getClubType();
        this.belongs = club.getBelongs();
        this.briefActivityDescription = club.getBriefActivityDescription();
        this.name = club.getName();
        this.headLine = club.getHeadLine();
        this.establishAt = club.getEstablishAt();
        this.roomLocation = club.getRoomLocation();
        this.memberAmount = club.getMemberAmount();
        this.regularMeetingTime = club.getRegularMeetingTime();
        this.mandatoryActivatePeriod = club.getMandatoryActivatePeriod();
        this.clubDescription = club.getClubDescription();
        this.activityDescription = club.getActivityDescription();
        this.logo = club.getLogo();
        this.activityImages = club.getActivityImages();
        this.webLink1 = club.getWebLink1();
        this.webLink2 = club.getWebLink2();
        this.president = club.getPresident();
    }
}
