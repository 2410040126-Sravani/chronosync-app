package com.chronosync.controller;

import com.chronosync.dto.LoginRequest;
import com.chronosync.dto.LoginResponse;
import com.chronosync.model.User;
import com.chronosync.model.Subscription;
import com.chronosync.repository.UserRepository;
import com.chronosync.repository.SubscriptionRepository;
import com.chronosync.security.JwtUtil;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SubscriptionRepository subscriptionRepository;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          SubscriptionRepository subscriptionRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.subscriptionRepository = subscriptionRepository;
    }

    // ================= REGISTER =================
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, Object> body) {

        String email = (String) body.get("email");
        String password = (String) body.get("password");
        String name = (String) body.get("name");
        String role = (String) body.get("role");

        if (email == null || password == null) {
            return ResponseEntity.badRequest().body("Missing email or password");
        }

        if (userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.badRequest().body("Email already exists");
        }

        // ✅ create user
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setName(name);
        user.setRole(role != null ? role : "CUSTOMER");

        User savedUser = userRepository.save(user);

        // 🔥 create subscription
        Subscription sub = new Subscription();
        sub.setCustomerId(savedUser.getId());
        sub.setCustomerName(savedUser.getName());
        sub.setQtyLitres(1);
        sub.setStatus("ACTIVE");
        sub.setEndDate(java.time.LocalDate.now().plusDays(30));

        Subscription savedSub = subscriptionRepository.save(sub);

        // 🔥 token
        String token = jwtUtil.generateToken(savedUser.getEmail(), savedUser.getRole());

        // 🔥 response
        LoginResponse response = new LoginResponse(
                token,
                savedUser.getRole(),
                savedUser.getId(),
                savedUser.getName(),
                savedSub.getId()
        );

        return ResponseEntity.ok(response);
    }

    // ================= LOGIN =================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // 🔥 FIXED: use customerId
            Subscription sub = subscriptionRepository.findByCustomerId(user.getId())
                    .orElseThrow(() -> new RuntimeException("Subscription not found"));

            String token = jwtUtil.generateToken(userDetails.getUsername(), user.getRole());

            LoginResponse response = new LoginResponse(
                    token,
                    user.getRole(),
                    user.getId(),
                    user.getName(),
                    sub.getId()
            );

            return ResponseEntity.ok(response);

        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid email or password");
        }
    }
}