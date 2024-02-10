package com.skklub.admin.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Thumbnail extends FileName{
    public Thumbnail(String originalName, String uploadedName) {
        super(originalName, uploadedName);
    }
}
