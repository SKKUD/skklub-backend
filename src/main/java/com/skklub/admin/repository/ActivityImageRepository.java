package com.skklub.admin.repository;

import com.skklub.admin.domain.ActivityImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ActivityImageRepository extends JpaRepository<ActivityImage, Long> {
    @Query("select a from ActivityImage a where a.originalName = :originalName and a.club.id = :clubId")
    Optional<ActivityImage> findByClubIdAndOriginalName(Long clubId, String originalName);
}
