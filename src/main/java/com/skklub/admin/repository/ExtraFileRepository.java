package com.skklub.admin.repository;

import com.skklub.admin.domain.ExtraFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExtraFileRepository extends JpaRepository<ExtraFile, Long> {
    Optional<ExtraFile> findByOriginalName(String originalName);
}
