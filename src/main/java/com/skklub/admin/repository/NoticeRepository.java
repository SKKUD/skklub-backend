package com.skklub.admin.repository;

import com.skklub.admin.domain.Notice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    @EntityGraph(attributePaths = {"extraFiles", "president", "logo", "activityImages"})
    Optional<Notice> findDetailById(Long id);
}
