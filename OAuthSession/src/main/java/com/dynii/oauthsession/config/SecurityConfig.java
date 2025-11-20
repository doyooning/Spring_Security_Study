package com.dynii.oauthsession.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // 개발환경에서는 csrf 꺼준다
        http
                .csrf((csrf) -> csrf.disable());

        // 폼 로그인 방식도 구현 안되어있으니 꺼준다
        http
                .formLogin((login) -> login.disable());

        http
                .httpBasic((basic) -> basic.disable());

        // oauth2Login은 기본적으로 다 구현이 되어있음
        // oauth2Client는 인증 인가 과정을 다 구현해주어야 함
        http
                .oauth2Login(Customizer.withDefaults());

        http
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/", "/oauth2/**", "/login/**").permitAll()
                        .anyRequest().authenticated());

        return http.build();
    }
}
