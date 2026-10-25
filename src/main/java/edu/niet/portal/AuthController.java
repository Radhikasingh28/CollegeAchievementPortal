
package edu.niet.portal;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;

    public AuthController(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        try {
            // Check username and password
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.username(),
                            request.password()));

            // Check whether the selected role matches the account
            String requestedRole = request.role();

            if (requestedRole == null ||
                    authentication.getAuthorities().stream()
                            .noneMatch(authority -> authority.getAuthority().equals(
                                    "ROLE_" + requestedRole.trim().toUpperCase()))) {

                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of(
                                "message",
                                "Selected role does not match your account."));
            }

            // Save authenticated user in the session
            SecurityContext context = SecurityContextHolder.createEmptyContext();

            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            httpRequest.getSession(true).setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    context);

            return ResponseEntity.ok(
                    Map.of(
                            "message", "Login successful",
                            "username", authentication.getName(),
                            "role", requestedRole.trim().toUpperCase()));

        } catch (org.springframework.security.core.AuthenticationException e) {

            SecurityContextHolder.clearContext();

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "message", "Invalid username or password."));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> currentUser(Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Not logged in."));
        }

        return ResponseEntity.ok(
                Map.of(
                        "username", authentication.getName(),
                        "roles", authentication.getAuthorities()));
    }
}
