package com.community.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import com.community.security.JwtFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // ---------------- CORS ----------------
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // ---------------- CSRF ----------------
            .csrf(csrf -> csrf.disable())

            // ---------------- Authorization ----------------
            .authorizeHttpRequests(auth -> auth

                // ---------- PUBLIC ----------
                .requestMatchers(
                    "/api/auth/**",
                    "/images/**"
                ).permitAll()

                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // ---------- PUBLIC RESOURCE VIEW ----------
                .requestMatchers(HttpMethod.GET, "/api/resources/public").permitAll()

                // ---------- RESOURCE DETAILS (VIEW) ----------
                .requestMatchers(HttpMethod.GET, "/api/resources/**")
                    .hasAnyAuthority("USER", "OWNER", "ADMIN")

                // ---------- CHAT & USER ----------
                .requestMatchers(
                    "/api/conversations/**",
                    "/api/messages/**",
                    "/api/user/**"
                ).hasAnyAuthority("USER", "OWNER", "ADMIN")

                // ---------- OWNER ----------
                .requestMatchers(
                    "/api/resources/owner",
                    "/api/resources/add",
                    "/api/resources/update/**",
                    "/api/resources/delete/**"
                ).hasAuthority("OWNER")

                // ---------- ADMIN ----------
                .requestMatchers("/api/admin/**").hasAuthority("ADMIN")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // ---------- FALLBACK ----------
                .anyRequest().authenticated()
            )

            // ---------------- Stateless session ----------------
            .sessionManagement(sess ->
                sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // ---------------- Exception handling ----------------
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(
                    (req, res, authEx) -> res.sendError(401, "Unauthorized")
                )
                .accessDeniedHandler(
                    (req, res, accessEx) -> res.sendError(403, "Forbidden")
                )
            );

        // ---------------- JWT Filter ----------------
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ---------------- CORS ----------------
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(List.of("http://localhost:3000"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
