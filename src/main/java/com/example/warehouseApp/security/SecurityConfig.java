package com.example.warehouseApp.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/reports/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/analytics/expiry-control").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/stock-balances").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/nomenclature/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/counterparty/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/warehouse/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/storageLocation/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/serial-item/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/batch/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/movements/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/inventory/checks/**").authenticated()

                        .requestMatchers(HttpMethod.POST, "/api/nomenclature").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.PATCH, "/api/nomenclature/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/nomenclature/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/counterparty").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.PATCH, "/api/counterparty/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/counterparty/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/warehouse").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.PATCH, "/api/warehouse/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/warehouse/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/storageLocation").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.PATCH, "/api/storageLocation/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/storageLocation/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/serial-item").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.PATCH, "/api/serial-item/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/serial-item/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/batch").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.PATCH, "/api/batch/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/batch/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/inventory/checks").hasAnyRole("ADMIN", "STOREKEEPER")
                        .requestMatchers(HttpMethod.POST, "/api/inventory/checks/*/complete").hasAnyRole("ADMIN", "STOREKEEPER")
                        .requestMatchers(HttpMethod.PUT, "/api/inventory/lines/*/actual-qty").hasAnyRole("ADMIN", "STOREKEEPER")

                        .requestMatchers(HttpMethod.POST, "/api/receipt").hasAnyRole("ADMIN", "STOREKEEPER")
                        .requestMatchers(HttpMethod.POST, "/api/issue").hasAnyRole("ADMIN", "STOREKEEPER")

                        .requestMatchers(HttpMethod.POST, "/api/writeoff").hasAnyRole("ADMIN", "STOREKEEPER")

                        .requestMatchers(HttpMethod.POST, "/api/transfer").hasAnyRole("ADMIN", "STOREKEEPER")

                    //    .requestMatchers(HttpMethod.POST, "/api/acts/*/settle").hasAnyRole("ADMIN", "MANAGER", "STOREKEEPER")

                        .requestMatchers(HttpMethod.POST, "/api/sync").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.DELETE, "/api/movements/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                .formLogin(AbstractAuthenticationFilterConfigurer::permitAll)
                .logout(LogoutConfigurer::permitAll);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}