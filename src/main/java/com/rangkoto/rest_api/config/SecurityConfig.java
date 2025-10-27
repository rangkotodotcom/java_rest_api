package com.rangkoto.rest_api.config;

import com.rangkoto.rest_api.security.JwtAccessFilter;
import com.rangkoto.rest_api.security.JwtGlobalFilter;
import com.rangkoto.rest_api.security.JwtRefreshFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtGlobalFilter globalFilter;
    private final JwtAccessFilter accessFilter;
    private final JwtRefreshFilter refreshFilter;

    public SecurityConfig(JwtGlobalFilter globalFilter,
                          JwtAccessFilter accessFilter,
                          JwtRefreshFilter refreshFilter) {
        this.globalFilter = globalFilter;
        this.accessFilter = accessFilter;
        this.refreshFilter = refreshFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Semua path tetap harus lewat filter, jadi permitAll() untuk semua, filter yang validasi
                        .anyRequest().permitAll()
                );

        // Urutan filter penting: Global -> Refresh -> Access
        http.addFilterBefore(globalFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(refreshFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(accessFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
