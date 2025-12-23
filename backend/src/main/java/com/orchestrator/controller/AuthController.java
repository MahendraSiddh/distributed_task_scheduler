package com.orchestrator.controller;

import com.orchestrator.entity.User;
import com.orchestrator.entity.UserRole;
import com.orchestrator.service.AuthService;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            User user = authService.register(
                request.getUsername(),
                request.getPassword(),
                request.getFullName(),
                request.getEmail(),
                request.getRole()
            );
            
            Map<String, Object> response = createUserResponse(user);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            Optional<User> userOpt = authService.login(request.getUsername(), request.getPassword());
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                Map<String, Object> response = createUserResponse(user);
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid username or password"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable Long userId) {
        try {
            User user = authService.getUserById(userId);
            return ResponseEntity.ok(createUserResponse(user));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    private Map<String, Object> createUserResponse(User user) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("username", user.getUsername());
        response.put("fullName", user.getFullName());
        response.put("email", user.getEmail());
        response.put("role", user.getRole());
        response.put("status", user.getStatus());
        return response;
    }
}

@Data
@Getter @Setter @NoArgsConstructor
class RegisterRequest {
    private String username;
    private String password;
    private String fullName;
    private String email;
    private UserRole role;
}

@Data
@Getter @Setter @NoArgsConstructor
class LoginRequest {
    private String username;
    private String password;
}