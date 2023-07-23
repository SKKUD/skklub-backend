package com.skklub.admin.domain;

import com.skklub.admin.domain.enums.Campus;
import com.skklub.admin.domain.enums.ClubType;
import com.skklub.admin.domain.enums.Role;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PendingClub extends BaseTimeEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pending_club_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    private Role requestTo;

    private String clubName;
    private String briefActivityDescription;
    @Lob
    private String activityDescription;
    @Lob
    private String clubDescription;

    private String username;
    private String password;
    private String presidentName;
    private String presidentContact;

    public PendingClub(String clubName, String briefActivityDescription, String activityDescription, String clubDescription, String username, String password, String presidentName, String presidentContact, Role role) {
        this.clubName = clubName;
        this.briefActivityDescription = briefActivityDescription;
        this.activityDescription = activityDescription;
        this.clubDescription = clubDescription;
        this.username = username;
        this.password = password;
        this.presidentName = presidentName;
        this.presidentContact = presidentContact;
        this.requestTo = role;
    }

    public Club toClubWithDefaultLogo(Campus campus, ClubType clubType, String belongs, User user) {
        Club club = new Club(
                clubName,
                activityDescription,
                belongs,
                clubType,
                briefActivityDescription,
                campus,
                clubDescription,
                user
        );
        Logo logo = new Logo("alt.jpg", "alt.jpg");
        club.changeLogo(logo);
        return club;
    }

    public User toUser() {
        return new User(
                username,
                password,
                Role.ROLE_USER,
                presidentName,
                presidentContact
        );
    }
}
