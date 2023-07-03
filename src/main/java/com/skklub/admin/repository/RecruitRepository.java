package com.skklub.admin.repository;

import com.skklub.admin.domain.Recruit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RecruitRepository extends JpaRepository<Recruit, Long> {
    @Query(value = "select r from Club c inner join c.recruit r where c.id = :clubId")
    Optional<Recruit> findByClubId(@Param("clubId") Long clubId);
}
