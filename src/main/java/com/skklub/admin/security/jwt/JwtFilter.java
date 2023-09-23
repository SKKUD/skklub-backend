package com.skklub.admin.security.jwt;

import com.skklub.admin.security.auth.PrincipalDetailsService;
import com.skklub.admin.security.redis.RedisUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final PrincipalDetailsService principalDetailsService;
    private final RedisUtil redisUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        try {
            String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
            log.info("authorization : {}", authorization);

            //token 미포함 시
            if (authorization == null) {
                log.warn("token not included");
                filterChain.doFilter(request, response);
                return;
            }

            //token 접두사 "Bearer " 누락
            if (!authorization.startsWith("Bearer ")) {
                log.error("invalid token : wrong token prefix");
                response.sendError(401, "SignatureException error");
                return;
            }

            //token 파싱
            String token = TokenProvider.resolveToken(authorization);

            if(redisUtil.hasKeyBlackList(token)){
                log.error("invalid token : This token has been already logged out");
                response.sendError(401, "SignatureException error");
                return;
            }

            //Username 미포함 or RT로 인증 시도
            if(TokenProvider.getUsername(token)==null){
                log.error("invalid token : username is null");
                response.sendError(401, "SignatureException error");
                return;
            }

            //username 추출
            String username = TokenProvider.getUsername(token);
            UserDetails userDetails = principalDetailsService.loadUserByUsername(username);
            //Authentication 발급
            UsernamePasswordAuthenticationToken AuthenticationToken = TokenProvider.getAuthentication(userDetails);
            AuthenticationToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );
            //최종 인증 완료
            SecurityContextHolder.getContext().setAuthentication(AuthenticationToken);
            filterChain.doFilter(request, response);

        } catch (SignatureException | MalformedJwtException e) {
            log.error("invalid token : malformed token");
            response.sendError(401, "SignatureException error");
        } catch (ExpiredJwtException e) {
            log.error("authorization expired");
            response.sendError(401, "ExpiredJwtException error");
        } catch (Exception e) {
            log.error("error occurred while processing token validation");
            response.sendError(401, "error occurred while processing token validation");
            SecurityContextHolder.clearContext();
        }
    }
}
