package com.rev.app.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Minimal security configuration for @WebMvcTest slices.
 * Permits all requests so that controller tests are not blocked by JWT
 * authentication. JWT-related beans (JwtAuthenticationFilter, JwtUtil) are
 * NOT registered here; controllers under test should mock them as @MockBean
 * for their own needs only.
 */
@TestConfiguration
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    @Bean
    public com.rev.app.security.JwtUtil jwtUtil() {
        return new com.rev.app.security.JwtUtil();
    }
}
