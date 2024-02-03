package com.skklub.admin.domain.imagefile;

import com.skklub.admin.domain.Club;
import com.skklub.admin.domain.FileName;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@EqualsAndHashCode(exclude = "club", callSuper = false)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ActivityImage extends FileName {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "activity_image_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    private Club club;

    public ActivityImage(String originalName, String uploadedName) {
        super(originalName, uploadedName);
    }

    public void matchToClub(Club club) {
        this.club = club;
    }
}
