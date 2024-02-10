package com.skklub.admin.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ActivityImage extends FileName{
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
