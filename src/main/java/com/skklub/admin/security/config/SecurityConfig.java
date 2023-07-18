package com.skklub.admin.security.config;


import com.skklub.admin.security.auth.PrincipalDetailsService;
import com.skklub.admin.security.jwt.JwtFilter;
import com.skklub.admin.security.redis.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig  {

    private final PrincipalDetailsService principalDetailsService;
    private final RedisUtil redisUtil;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true);
        config.setAllowedOrigins(Arrays.asList("*"));
        config.setAllowedMethods(Arrays.asList("*"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public BCryptPasswordEncoder encodePwd(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .exceptionHandling()
                .and()
                .cors().configurationSource(corsConfigurationSource())
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .formLogin().disable()
                .httpBasic().disable()
                .authorizeHttpRequests(authorize -> {
                    try {
                        authorize
                                .requestMatchers(userEndpoints()).access(new WebExpressionAuthorizationManager("hasRole('ROLE_MASTER') or hasRole('ROLE_ADMIN_SEOUL_CENTRAL') or hasRole('ROLE_ADMIN_SUWON_CENTRAL') or hasRole('ROLE_USER')"))
                                .requestMatchers(adminEndpoints()).access(new WebExpressionAuthorizationManager("hasRole('ROLE_MASTER') or hasRole('ROLE_ADMIN_SEOUL_CENTRAL') or hasRole('ROLE_ADMIN_SUWON_CENTRAL')"))
                                .requestMatchers(masterEndpoints()).access(new WebExpressionAuthorizationManager("hasRole('ROLE_MASTER')"))
                                .anyRequest().permitAll();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
        )
        .addFilterBefore(new JwtFilter(principalDetailsService, redisUtil), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    private RequestMatcher userEndpoints() {
        return new OrRequestMatcher(
                new AntPathRequestMatcher("/user/**"), //update
                new AntPathRequestMatcher("/user/logout"),
                new AntPathRequestMatcher("/refresh")
        );
    }
    private RequestMatcher adminEndpoints() {
        return new OrRequestMatcher(
                new AntPathRequestMatcher("/admin/**")
        );
    }

    private RequestMatcher masterEndpoints() {
        return new OrRequestMatcher(
                new AntPathRequestMatcher("/master/**")
        );
    }

}
