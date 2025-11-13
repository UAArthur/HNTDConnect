package net.hauntedstudio.hntdconnect.controller;

import jakarta.validation.Valid;
import net.hauntedstudio.hntdconnect.dto.auth.*;
import net.hauntedstudio.hntdconnect.entities.UserEntity;
import net.hauntedstudio.hntdconnect.entities.UserSessionEntity;
import net.hauntedstudio.hntdconnect.services.JwtService;
import net.hauntedstudio.hntdconnect.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

//TODO: Potential way of making tokens more Secure.
// - Bind token to the IP address, or somewhat compare the ip or something of the user on every request to make sure the token was not stolen???
// cons: Kinda performance heavy I would guess, and im not sure in storing the users IP
// Hash IP, store hash in token, compare on every request??? That may actually work, still performance heavy


@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtService jwtService;
    private final UserService userService;

    public AuthController(JwtService jwtService, UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            UserEntity user = userService.registerUser(registerRequest);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "User registered successfully", "uuid", user.getUuid()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Registration failed"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        if (!userService.validateCredentials(loginRequest.username(), loginRequest.password())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid credentials"));
        }

        try {
            Optional<UserEntity> userOpt = userService.findByUsername(loginRequest.username());
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "User not found"));
            }



            UserEntity user = userOpt.get();
            UserSessionEntity session = jwtService.createSession(user.getUuid());

            AuthResponse response = new AuthResponse(session.getRawAccessToken(), session.getRawRefreshToken());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("Error during login: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Login failed"));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshTokenRequest request) {
        try {
            Optional<UserSessionEntity> sessionOpt = jwtService.getActiveSessionByRefreshToken(request.refreshToken());

            if (sessionOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid refresh token"));
            }

            UserSessionEntity session = sessionOpt.get();
            UserSessionEntity newSession = jwtService.createSession(session.getUserUuid());

            TokenResponse response = new TokenResponse(newSession.getAccessToken());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Token refresh failed"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody LogoutRequest request) {
        try {
            jwtService.deactivateSession(request.refreshToken());
            return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Logout failed"));
        }
    }
}
