package com.skklub.admin.repository;

import com.skklub.admin.domain.imagefile.ExtraFile;
import com.skklub.admin.domain.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExtraFileRepository extends JpaRepository<ExtraFile, Long> {
    Optional<ExtraFile> findByOriginalNameAndNotice(String originalName, Notice notice);
}
