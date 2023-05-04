package com.skklub.admin.repository;

import com.skklub.admin.domain.Club;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.util.Optional;

public interface ClubRepository extends JpaRepository<Club, Long> {
    @EntityGraph(attributePaths = {"recruit", "president", "logo", "activityImages"})
    Optional<Club> findDetailClubById(Long id);
}
