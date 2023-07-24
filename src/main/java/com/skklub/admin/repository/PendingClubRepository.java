package com.skklub.admin.repository;

import com.skklub.admin.domain.PendingClub;
import com.skklub.admin.domain.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PendingClubRepository extends JpaRepository<PendingClub, Long> {
    Page<PendingClub> findAllByRequestTo(Role requestTo, Pageable pageable);
}
