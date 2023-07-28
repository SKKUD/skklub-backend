package com.skklub.admin.repository;

import com.skklub.admin.domain.Club;
import com.skklub.admin.domain.enums.Campus;
import com.skklub.admin.domain.enums.ClubType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClubRepository extends JpaRepository<Club, Long> {
    @EntityGraph(attributePaths = {"recruit", "president", "logo", "activityImages"})
    Optional<Club> findDetailClubById(Long id);

    @EntityGraph(attributePaths = {"recruit", "president", "logo", "activityImages"})
    Optional<Club> findDetailClubByName(String name);

    @EntityGraph(attributePaths = {"logo"})
    Page<Club> findClubByCampusAndClubTypeAndBelongs(Campus campus, ClubType clubType, String belongs, Pageable pageable);

    @EntityGraph(attributePaths = {"logo"})
    Page<Club> findClubByCampusAndClubType(Campus campus, ClubType clubType, Pageable pageable);

    @EntityGraph(attributePaths = {"logo"})
    Page<Club> findClubByCampus(Campus campus, Pageable pageable);

    @EntityGraph(attributePaths = {"logo"})
    Page<Club> findClubByNameContaining(String name, Pageable pageable);

    @Query(value = "select * from club where campus = :campus and club_type = :clubType and belongs = :belongs order by rand() limit 3", nativeQuery = true)
    List<Club> findClubRandomByCategories(@Param("campus") String campus, @Param("clubType") String clubType, @Param("belongs") String belongs);

    @Query(value = "select * from club where campus = :campus and club_type = :clubType order by rand() limit 3", nativeQuery = true)
    List<Club> findClubRandomByCategories(@Param("campus") String campus, @Param("clubType") String clubType);

    @Query(value = "select * from club where campus = :campus order by rand() limit 3", nativeQuery = true)
    List<Club> findClubRandomByCategories(@Param("campus") String campus);

    @Query(value = "select club.user_id from club join recruit on club.recruit_id = recruit.recruit_id where recruit.recruit_id = :recruitId", nativeQuery = true)
    List<Long> findClubPresidentIdByRecruitId(@Param("recruitId") Long recruitId);

}

