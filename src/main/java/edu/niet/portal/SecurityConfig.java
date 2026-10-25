package edu.niet.portal;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/index.html", "/styles.css",
                                 "/app.js", "/api/auth/login").permitAll()
                .requestMatchers("/api/auth/me").authenticated()
                .requestMatchers(HttpMethod.PATCH,
                                 "/api/achievements/*/review")
                    .hasAnyRole("FACULTY", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/achievements")
                    .hasAnyRole("STUDENT", "ADMIN")
                .requestMatchers("/api/**").authenticated()
                .anyRequest().permitAll()
            );

        return http.build();
    }
}
