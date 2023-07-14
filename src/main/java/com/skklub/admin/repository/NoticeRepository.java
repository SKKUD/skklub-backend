package com.skklub.admin.repository;

import com.skklub.admin.domain.Notice;
import com.skklub.admin.domain.enums.Role;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    @Override
    @EntityGraph(attributePaths = {"writer"})
    Page<Notice> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"writer", "extraFiles"})
    Optional<Notice> findDetailById(Long id);

    @Query(value = "select n from Notice n where n.createdAt < :createdAt order by n.createdAt desc limit 1")
    Optional<Notice> findPreByCreatedAt(@Param("createdAt") LocalDateTime createdAt);

    @Query(value = "select n from Notice n where n.createdAt > :createdAt order by n.createdAt asc limit 1")
    Optional<Notice> findPostByCreatedAt(@Param("createdAt") LocalDateTime createdAt);

    @EntityGraph(attributePaths = {"writer", "thumbnail"})
    Page<Notice> findAllWithThumbnailBy(Pageable pageable);

    @EntityGraph(attributePaths = {"writer"})
    Page<Notice> findWithWriterAllByTitleContainingOrderByCreatedAt(String title, Pageable pageable);

    @Query(value = "select n from Notice n inner join n.writer where n.writer.role = :role")
    @EntityGraph(attributePaths = {"writer"})
    Page<Notice> findAllByUserRole(@Param("role") Role role, Pageable pageable);

}
