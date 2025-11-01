package com.ssg.testsecurity.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain (HttpSecurity http) throws Exception {
        http.authorizeHttpRequests( // !!! 상단부터 순차 실행 -> 맨 위에서 permitAll 하면 밑에는 소용없다
                (auth) -> auth
                        .requestMatchers("/", "/login").permitAll() // 메인페이지, 로그인페이지는 모두에게
                        .requestMatchers("/admin").hasRole("ADMIN")
                        .requestMatchers("/my/**").hasAnyRole("USER", "ADMIN")
                        .anyRequest().authenticated() // 위 요청 외에는 인가된 사용자만
        );
        return http.build();
    }
}
