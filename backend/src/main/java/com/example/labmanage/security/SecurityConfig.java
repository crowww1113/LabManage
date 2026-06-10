package com.example.labmanage.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 排课相关 API 需要认证
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/login.html", "/index.html", "/favicon.ico", "/h2-console/**").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/static/**").permitAll()
                        .requestMatchers("/*.html", "/*.js", "/*.css", "/*.ico").permitAll()
                        .requestMatchers("/api-fetch.js").permitAll()
                        .requestMatchers("/api/**").permitAll()
                        .requestMatchers("/export/**").permitAll()
                        .anyRequest().authenticated())
                // 未认证返回 401 JSON
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write("{\"code\":401,\"message\":\"未登录或登录已过期\"}");
                        }))
                // 禁用 CSRF
                .csrf(csrf -> csrf.disable())
                // 无状态会话（JWT 不需要服务端 session）
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 禁用表单登录
                .formLogin(form -> form.disable())
                // 禁用 HTTP Basic 认证
                .httpBasic(basic -> basic.disable())
                // 允许 iframe 加载
                .headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }
}