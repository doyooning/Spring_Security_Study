package com.ssg.testsecurity.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain (HttpSecurity http) throws Exception { // !!! 상단부터 순차 실행 -> 맨 위에서 permitAll 하면 밑에는 소용없다
        http.authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/", "/login", "/loginProc", "/join", "/joinProc").permitAll() // 메인페이지, 로그인페이지는 모두에게
                        .requestMatchers("/admin").hasRole("ADMIN")
                        .requestMatchers("/my/**").hasAnyRole("USER", "ADMIN")
                        .anyRequest().authenticated() // 위 요청 외에는 인가된 사용자만
        );

        http.formLogin((auth) -> auth.loginPage("/login") // 로그인 페이지를 연결해줌
                        .loginProcessingUrl("/loginProc") // 로그인이 완료되면 연결할 페이지
                        .permitAll() // 로그인되면 permitAll 상태

        );
        // csrf 설정 - 지금은 비활성화로 (로그인이 안됨)
        http.csrf((auth) -> auth.disable());

        http.sessionManagement((auth) -> auth
                        .maximumSessions(5)
                        .maxSessionsPreventsLogin(true));

        // 세션 고정 설정 -> 설정안함, 새 세션 생성, [아이디만 변경]
        http.sessionManagement((auth) -> auth
                    .sessionFixation().changeSessionId());

        return http.build();
    }
}
