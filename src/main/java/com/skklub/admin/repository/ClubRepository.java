package com.skklub.admin.repository;

import com.skklub.admin.domain.Club;
import com.skklub.admin.domain.enums.Campus;
import com.skklub.admin.domain.enums.ClubType;
import com.skklub.admin.repository.dto.ClubPrevDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ClubRepository extends JpaRepository<Club, Long> {
    @EntityGraph(attributePaths = {"recruit", "president", "logo", "activityImages"})
    Optional<Club> findDetailClubById(Long id);

    @Query(value = """
            select 
            new com.skklub.admin.repository.dto.ClubPrevDTO(c.id, c.name, c.belongs, c.briefActivityDescription, l.originalName, l.uploadedName) 
            from Club c 
            left join c.logo l
            where c.campus = :campus and c.clubType = :clubType and c.belongs = :belongs
            """, countQuery = "select count(c) from Club c")
    Page<ClubPrevDTO> findClubPrevs(@Param("campus") Campus campus, @Param("clubType") ClubType clubType, @Param("belongs") String belongs, Pageable pageable);

    @Query(value = """
            select 
            new com.skklub.admin.repository.dto.ClubPrevDTO(c.id, c.name, c.belongs, c.briefActivityDescription, l.originalName, l.uploadedName) 
            from Club c 
            left join c.logo l
            where c.campus = :campus and c.clubType = :clubType
            """, countQuery = "select count(c) from Club c")
    Page<ClubPrevDTO> findClubPrevs(@Param("campus") Campus campus, @Param("clubType") ClubType clubType, Pageable pageable);

    @Query(value = """
            select 
            new com.skklub.admin.repository.dto.ClubPrevDTO(c.id, c.name, c.belongs, c.briefActivityDescription, l.originalName, l.uploadedName) 
            from Club c 
            left join c.logo l
            where c.campus = :campus
            """, countQuery = "select count(c) from Club c")
    Page<ClubPrevDTO> findClubPrevs(@Param("campus") Campus campus, Pageable pageable);
}
