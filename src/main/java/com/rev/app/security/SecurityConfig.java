package com.rev.app.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;

        @Bean
        public org.springframework.security.authentication.AuthenticationManager authenticationManager(
                        org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration config)
                        throws Exception {
                return config.getAuthenticationManager();
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

                http
                                .csrf(csrf -> csrf.disable())
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/", "/products", "/products/{id:[0-9]+}",
                                                                "/category/**", "/register", "/login", "/css/**",
                                                                "/js/**", "/images/**", "/uploads/**", "/search",
                                                                "/api/auth/**", "/api/**", "/error", "/get-started")
                                                .permitAll()
                                                .requestMatchers("/orders/**").authenticated()
                                                .requestMatchers("/products/add", "/products/save", "/products/edit/**",
                                                                "/products/update", "/products/delete/**",
                                                                "/products/seller")
                                                .hasRole("SELLER")
                                                .requestMatchers("/notifications/**", "/profile/**").authenticated()
                                                .requestMatchers("/buyer/**").hasRole("BUYER")
                                                .anyRequest().authenticated())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)) // Maintain
                                                                                                           // session
                                                                                                           // for web UI
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .defaultSuccessUrl("/", true)
                                                .permitAll())
                                .logout(logout -> logout
                                                .logoutSuccessUrl("/login?logout")
                                                .permitAll())
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }
}