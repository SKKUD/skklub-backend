package com.skklub.admin.repository;

import com.skklub.admin.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface UserRepository extends JpaRepository<User,Long> {

    User findByUsername(String username);
}
