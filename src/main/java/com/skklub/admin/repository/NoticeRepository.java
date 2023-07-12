package com.skklub.admin.repository;

import com.skklub.admin.domain.Notice;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    @EntityGraph(attributePaths = {"writer", "extraFiles"})
    Optional<Notice> findDetailById(Long id);

    @Query(value = "select n from Notice n where n.createdAt < :createdAt order by n.createdAt desc limit 1")
    Optional<Notice> findTopByByCreatedAtLessThanEqualOOrderByCreatedAtDesc(@Param("createdAt") LocalDateTime createdAt);

    @Query(value = "select n from Notice n where n.createdAt > :createdAt order by n.createdAt asc limit 1")
    Optional<Notice> findTopByCreatedAtGreaterThanEqualOrderByCreatedAtAsc(LocalDateTime createdAt);
}
