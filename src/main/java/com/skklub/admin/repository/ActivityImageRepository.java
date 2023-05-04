package com.skklub.admin.repository;

import com.skklub.admin.domain.ActivityImage;
import com.skklub.admin.domain.Club;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityImageRepository extends JpaRepository<ActivityImage, Long> {
    public List<ActivityImage> findAllByClub(Club club);
}
