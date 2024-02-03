package com.skklub.admin.repository;

import com.skklub.admin.domain.imagefile.ActivityImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ActivityImageRepository extends JpaRepository<ActivityImage, Long> {
    @Query("select a from ActivityImage a where a.originalName = :originalName and a.club.id = :clubId")
    Optional<ActivityImage> findByClubIdAndOriginalName(@Param("clubId") Long clubId, @Param("originalName") String originalName);
}
