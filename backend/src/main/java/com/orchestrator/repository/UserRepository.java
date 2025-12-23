package com.orchestrator.repository;

import com.orchestrator.entity.User;
import com.orchestrator.entity.UserRole;
import com.orchestrator.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findByRole(UserRole role);
    List<User> findByRoleAndStatus(UserRole role, UserStatus status);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}