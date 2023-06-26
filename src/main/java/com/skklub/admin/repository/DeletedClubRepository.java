package com.skklub.admin.repository;

import com.skklub.admin.domain.DeletedClub;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeletedClubRepository extends JpaRepository<DeletedClub, Long> {
}
