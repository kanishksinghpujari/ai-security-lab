package dev.kanishk.aisecuritylab.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/index.html", "/app.js", "/styles.css", "/actuator/health").permitAll()
                .anyRequest().authenticated())
            .httpBasic(Customizer.withDefaults())
            .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Profile("!production")
    UserDetailsService demoUsers(
        PasswordEncoder encoder,
        @Value("${LAB_ADMIN_PASSWORD:change-this-admin-password}") String adminPassword,
        @Value("${LAB_CUSTOMER_PASSWORD:change-this-customer-password}") String customerPassword) {
        var admin = User.withUsername("lab-admin")
            .password(encoder.encode(adminPassword)).roles("ADMIN", "USER").build();
        var customer = User.withUsername("lab-customer")
            .password(encoder.encode(customerPassword)).roles("USER").build();
        return new InMemoryUserDetailsManager(admin, customer);
    }
}
