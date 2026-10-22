package com.accommodation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Profile("!test")
    SecurityFilterChain applicationSecurity(HttpSecurity http) throws Exception {
        AuthenticationSuccessHandler successHandler = (request, response, authentication) -> {
            boolean student = authentication.getAuthorities().stream()
                    .anyMatch(authority -> "ROLE_STUDENT".equals(authority.getAuthority()));
            boolean mustChangePassword = authentication.getAuthorities().stream()
                    .anyMatch(authority -> "PASSWORD_CHANGE_REQUIRED".equals(authority.getAuthority()));
            if (mustChangePassword) {
                response.sendRedirect("/student/change-password");
                return;
            }
            response.sendRedirect(student ? "/student/fees" : "/dashboard");
        };
        return http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/login", "/css/**", "/js/**", "/favicon.ico").permitAll()
                        .requestMatchers("/student/change-password").hasRole("STUDENT")
                        .requestMatchers("/student/**")
                        .access(new WebExpressionAuthorizationManager(
                                "hasRole('STUDENT') and !hasAuthority('PASSWORD_CHANGE_REQUIRED')"))
                        .requestMatchers("/notifications/**", "/fees/receipts/**")
                        .access(new WebExpressionAuthorizationManager(
                                "hasAnyRole('ADMIN','STUDENT') and !hasAuthority('PASSWORD_CHANGE_REQUIRED')"))
                        .requestMatchers("/api/student/**").hasRole("STUDENT")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/**").hasRole("ADMIN")
                        .anyRequest().hasRole("ADMIN"))
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(successHandler)
                        .permitAll())
                .logout(logout -> logout.logoutSuccessUrl("/login?logout"))
                .httpBasic(basic -> {})
                .build();
    }

    @Bean
    @Profile("test")
    SecurityFilterChain testSecurity(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
                .csrf(csrf -> csrf.disable())
                .build();
    }
}
