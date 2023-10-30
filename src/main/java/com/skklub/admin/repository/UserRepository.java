package com.skklub.admin.repository;

import com.skklub.admin.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    User findByUsername(String username);
}
