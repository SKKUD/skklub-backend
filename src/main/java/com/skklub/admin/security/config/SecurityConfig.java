package com.skklub.admin.security.config;


import com.skklub.admin.security.auth.PrincipalDetailsService;
import com.skklub.admin.security.jwt.JwtFilter;
import com.skklub.admin.security.redis.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
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
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig  {

    private final PrincipalDetailsService principalDetailsService;
    private final RedisUtil redisUtil;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(Arrays.asList("http://localhost:3000", "https://skklub.com/", "https://www.skklub.com/"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setExposedHeaders(Arrays.asList("Authorization", "Refresh-Token"));
        config.setMaxAge(3600L);
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
                .httpBasic().disable()
                .csrf().disable()
                .cors().configurationSource(corsConfigurationSource())
                .and()
                .exceptionHandling()
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .formLogin().disable()
                .authorizeHttpRequests(authorize -> {
                    try {
                        authorize
                                .requestMatchers(publicEndpoints()).permitAll()
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

    private RequestMatcher publicEndpoints() {
        return new OrRequestMatcher(
                //user
                new AntPathRequestMatcher("/user/login","POST"),
                //pending
                new AntPathRequestMatcher("/pending/**","POST"),
                //notice
                new AntPathRequestMatcher("/notice/**","GET"),
                new AntPathRequestMatcher("/notice/**/**","GET"),
//                new AntPathRequestMatcher("/notice/prev","GET"),
//                new AntPathRequestMatcher("/notice/prev/thumbnail","GET"),
//                new AntPathRequestMatcher("/notice/prev/search/title","GET"),
                //club
                new AntPathRequestMatcher("/club/**}","GET"),
                new AntPathRequestMatcher("/club/prev","GET"),
                new AntPathRequestMatcher("/club/search","GET"),
                new AntPathRequestMatcher("/club/search/prevs","GET"),
                new AntPathRequestMatcher("/club/random","GET")
        );
    }

    private RequestMatcher userEndpoints() {
        return new OrRequestMatcher(
                //user
               new AntPathRequestMatcher("/user/**","POST"), //update
                new AntPathRequestMatcher("/user/logout","POST"),
                //refresh
                new AntPathRequestMatcher("/refresh","POST"),
                //notice
                new AntPathRequestMatcher("/notice/**","POST"),
                new AntPathRequestMatcher("/notice/**","PATCH"),
                new AntPathRequestMatcher("/notice/**","DELETE"),
                new AntPathRequestMatcher("/notice/**/**","DELETE"),
                //recruit
                new AntPathRequestMatcher("/recruit/**","POST"),
                new AntPathRequestMatcher("/recruit/**","PATCH"),
                new AntPathRequestMatcher("/recruit/**","DELETE"),
                //club
                new AntPathRequestMatcher("/club/**","PATCH"),
                new AntPathRequestMatcher("/club/**/logo","PATCH"),
                new AntPathRequestMatcher("/club/**/activityImage","DELETE"),
                new AntPathRequestMatcher("/club/**","DELETE")
        );

    }
    private RequestMatcher adminEndpoints() {
        return new OrRequestMatcher(
                //pending
                new AntPathRequestMatcher("/pending/**","GET"),
                new AntPathRequestMatcher("/pending/**","DELETE"),
                //club
                new AntPathRequestMatcher("/club/**/up","PATCH"),
                new AntPathRequestMatcher("/club/**/down","PATCH")
        );
    }

    private RequestMatcher masterEndpoints() {
        return new OrRequestMatcher(
                new AntPathRequestMatcher("/master")

        );
    }

}
