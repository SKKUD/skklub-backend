package com.skklub.admin.repository;

import com.skklub.admin.domain.Logo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LogoRepository extends JpaRepository<Logo, Long> {
    @Query("select l from Logo l inner join Club c where c.id = :clubId")
    Optional<Logo> findByClubId(@Param("clubId") Long clubId);
}
