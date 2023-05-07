package com.skklub.admin.repository;

import com.skklub.admin.domain.Club;
import com.skklub.admin.domain.enums.Campus;
import com.skklub.admin.domain.enums.ClubType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClubRepository extends JpaRepository<Club, Long> {
    @EntityGraph(attributePaths = {"recruit", "president", "logo", "activityImages"})
    Optional<Club> findDetailClubById(Long id);

    @EntityGraph(attributePaths = {"recruit", "president", "logo", "activityImages"})
    Optional<Club> findDetailClubByName(String name);

    @EntityGraph(attributePaths = {"logo"})
    Page<Club> findClubPrevByCampusAndClubTypeAndBelongsOrderByName(Campus campus, ClubType clubType, String belongs, Pageable pageable);

    @EntityGraph(attributePaths = {"logo"})
    Page<Club> findClubPrevByCampusAndClubTypeOrderByName(Campus campus, ClubType clubType, Pageable pageable);

    @EntityGraph(attributePaths = {"logo"})
    Page<Club> findClubPrevByCampusOrderByName(Campus campus, Pageable pageable);

    @EntityGraph(attributePaths = {"logo"})
    Page<Club> findClubPrevByNameContainingOrderByName(String name, Pageable pageable);
}
