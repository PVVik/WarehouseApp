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
@EnableMethodSecurity(prePostEnabled = true)
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
                        .requestMatchers(HttpMethod.POST, "/api/inventory/checks/{id}/complete").hasAnyRole("ADMIN", "STOREKEEPER")
                        .requestMatchers(HttpMethod.PUT, "/api/inventory/lines/{id}/actual-qty").hasAnyRole("ADMIN", "STOREKEEPER")

                        .requestMatchers(HttpMethod.POST, "/api/receipt").hasAnyRole("ADMIN", "STOREKEEPER")
                        .requestMatchers(HttpMethod.POST, "/api/issue").hasAnyRole("ADMIN", "STOREKEEPER")
                        .requestMatchers(HttpMethod.POST, "/api/writeoff").hasAnyRole("ADMIN", "STOREKEEPER")
                        .requestMatchers(HttpMethod.POST, "/api/transfer").hasAnyRole("ADMIN", "STOREKEEPER")

                        .requestMatchers(HttpMethod.POST, "/api/sync").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/movements/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/").authenticated()
                        .requestMatchers(HttpMethod.GET, "/analytics").authenticated()
                        .requestMatchers(HttpMethod.GET, "/analytics/expiry-control").authenticated()

                        .requestMatchers(HttpMethod.GET, "/counterparty").authenticated()
                        .requestMatchers(HttpMethod.GET, "/counterparty/create").hasAnyRole("ADMIN", "STOREKEEPER")
                        .requestMatchers(HttpMethod.GET, "/counterparty/{id}/edit").hasAnyRole("ADMIN", "STOREKEEPER")
                        .requestMatchers(HttpMethod.POST, "/counterparty/save").hasAnyRole("ADMIN", "STOREKEEPER")
                        .requestMatchers(HttpMethod.GET, "/counterparty/{id}/delete").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/inventory").authenticated()
                        .requestMatchers(HttpMethod.GET, "/inventory/create").hasAnyRole("ADMIN", "STOREKEEPER")
                        .requestMatchers(HttpMethod.POST, "/inventory").hasAnyRole("ADMIN", "STOREKEEPER")
                        .requestMatchers(HttpMethod.GET, "/inventory/{id}/lines").hasAnyRole("ADMIN", "STOREKEEPER")
                        .requestMatchers(HttpMethod.POST, "/inventory/{id}/lines").hasAnyRole("ADMIN", "STOREKEEPER")
                        .requestMatchers(HttpMethod.POST, "/inventory/{id}/complete").hasAnyRole("ADMIN", "STOREKEEPER")
                        .requestMatchers(HttpMethod.GET, "/inventory/{id}/acts").authenticated()
                        .requestMatchers(HttpMethod.POST, "/inventory/{inventoryId}/acts/{actId}/settle").hasAnyRole("ADMIN", "STOREKEEPER")
                        .requestMatchers(HttpMethod.GET, "/inventory/{id}/review").authenticated()

                        .requestMatchers(HttpMethod.GET, "/movements").authenticated()
                        .requestMatchers(HttpMethod.GET, "/movements/nomenclature/list").hasAnyRole("ADMIN", "STOREKEEPER")
                        .requestMatchers(HttpMethod.GET, "/movements/counterparty/list").hasAnyRole("ADMIN", "STOREKEEPER")
                        .requestMatchers(HttpMethod.GET, "/movements/warehouse/list").hasAnyRole("ADMIN", "STOREKEEPER")

                        .requestMatchers(HttpMethod.GET, "/nomenclature").authenticated()
                        .requestMatchers(HttpMethod.GET, "/nomenclature/form").hasAnyRole("ADMIN", "STOREKEEPER")
                        .requestMatchers(HttpMethod.POST, "/nomenclature/form").hasAnyRole("ADMIN", "STOREKEEPER")

                        .requestMatchers(HttpMethod.GET, "/report").authenticated()
                        .requestMatchers(HttpMethod.GET, "/report/stock-form").authenticated()
                        .requestMatchers(HttpMethod.GET, "/report/download-stock").authenticated()
                        .requestMatchers(HttpMethod.GET, "/report/stock-view").authenticated()
                        .requestMatchers(HttpMethod.GET, "/report/movement-form").authenticated()
                        .requestMatchers(HttpMethod.GET, "/report/download-movement").authenticated()
                        .requestMatchers(HttpMethod.GET, "/report/movement-view").authenticated()
                        .requestMatchers(HttpMethod.GET, "/report/turnover").authenticated()
                        .requestMatchers(HttpMethod.GET, "/report/deficit").authenticated()
                        .requestMatchers(HttpMethod.GET, "/report/nonliquid").authenticated()

                        .requestMatchers(HttpMethod.GET, "/storageLocation/create").hasAnyRole("ADMIN", "STOREKEEPER")
                        .requestMatchers(HttpMethod.GET, "/storageLocation/{id}/edit").hasAnyRole("ADMIN", "STOREKEEPER")
                        .requestMatchers(HttpMethod.POST, "/storageLocation/save").hasAnyRole("ADMIN", "STOREKEEPER")
                        .requestMatchers(HttpMethod.POST, "/storageLocation/{id}/delete").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/warehouse").authenticated()
                        .requestMatchers(HttpMethod.GET, "/warehouse/new").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/warehouse/{id}/edit").hasAnyRole("ADMIN", "STOREKEEPER")
                        .requestMatchers(HttpMethod.POST, "/warehouse").hasAnyRole("ADMIN", "STOREKEEPER")
                        .requestMatchers(HttpMethod.POST, "/warehouse/{id}/delete").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/warehouse/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/warehouse/{id}/locations/bulk").hasAnyRole("ADMIN", "STOREKEEPER")
                        .requestMatchers(HttpMethod.GET, "/warehouse/{id}/locations/bulk").hasAnyRole("ADMIN", "STOREKEEPER")

                        .requestMatchers(HttpMethod.GET, "/movements/receipt/form").hasAnyRole("ADMIN", "STOREKEEPER")
                        .requestMatchers(HttpMethod.POST, "/movements/receipt").hasAnyRole("ADMIN", "STOREKEEPER")
                        .requestMatchers(HttpMethod.GET, "/movements/issue/form").hasAnyRole("ADMIN", "STOREKEEPER")
                        .requestMatchers(HttpMethod.POST, "/movements/issue").hasAnyRole("ADMIN", "STOREKEEPER")
                        .requestMatchers(HttpMethod.GET, "/movements/transfer/form").hasAnyRole("ADMIN", "STOREKEEPER")
                        .requestMatchers(HttpMethod.POST, "/movements/transfer").hasAnyRole("ADMIN", "STOREKEEPER")
                        .requestMatchers(HttpMethod.GET, "/movements/write-off/form").hasAnyRole("ADMIN", "STOREKEEPER")
                        .requestMatchers(HttpMethod.POST, "/movements/write-off").hasAnyRole("ADMIN", "STOREKEEPER")

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
