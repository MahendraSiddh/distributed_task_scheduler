package com.orchestrator.service;

import com.orchestrator.entity.User;
import com.orchestrator.entity.UserRole;
import com.orchestrator.entity.UserStatus;
import com.orchestrator.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final UserRepository userRepository;
    private final EmployeeStatsService employeeStatsService;
    
    @Transactional
    public User register(String username, String password, String fullName, String email, UserRole role) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }
        
        User user = new User();
        user.setUsername(username);
        user.setPassword(hashPassword(password)); // In production, use BCrypt
        user.setFullName(fullName);
        user.setEmail(email);
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        
        user = userRepository.save(user);
        
        // Create stats entry for employees
        if (role == UserRole.EMPLOYEE) {
            employeeStatsService.initializeEmployeeStats(user);
        }
        
        log.info("User registered: {} with role: {}", username, role);
        return user;
    }
    
    public Optional<User> login(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getStatus() != UserStatus.ACTIVE) {
                throw new RuntimeException("User account is not active");
            }
            if (verifyPassword(password, user.getPassword())) {
                log.info("User logged in: {}", username);
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }
    
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    private String hashPassword(String password) {
        // In production, use BCryptPasswordEncoder
        // For now, simple hash (NOT SECURE - just for demo)
        return "hashed_" + password;
    }
    
    private boolean verifyPassword(String rawPassword, String hashedPassword) {
        // In production, use BCryptPasswordEncoder
        return hashedPassword.equals("hashed_" + rawPassword);
    }
}