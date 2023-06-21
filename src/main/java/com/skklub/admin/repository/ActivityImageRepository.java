package com.skklub.admin.repository;

import com.skklub.admin.domain.ActivityImage;
import com.skklub.admin.domain.Club;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ActivityImageRepository extends JpaRepository<ActivityImage, Long> {
    @Query("select a from ActivityImage a where a.originalName = :originalName and a.club.id = :clubId")
    Optional<ActivityImage> findByOriginalNameAndClubId(String originalName, Long clubId);
}
