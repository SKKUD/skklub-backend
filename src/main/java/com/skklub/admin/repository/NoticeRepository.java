package com.skklub.admin.repository;

import com.skklub.admin.domain.Notice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    @EntityGraph(attributePaths = {"writer", "extraFiles"})
    Optional<Notice> findDetailById(Long id);

    Optional<Notice> findTopByByCreatedAtLessThanEqualOrderByCreatedAtDesc(LocalDateTime createdAt);
    Optional<Notice> findTopByCreatedAtGreaterThanEqualOrderByCreatedAtAsc(LocalDateTime createdAt);
}
