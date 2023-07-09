package com.skklub.admin.repository;

import com.skklub.admin.domain.Notice;
import com.skklub.admin.domain.Thumbnail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ThumbnailRepository extends JpaRepository<Thumbnail, Long> {
}
