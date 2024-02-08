package com.skklub.admin.service;

import com.skklub.admin.domain.enums.Role;
import com.skklub.admin.exception.deprecated.ErrorCode;
import com.skklub.admin.exception.deprecated.InvalidTokenException;
import com.skklub.admin.security.auth.PrincipalDetailsService;
import com.skklub.admin.security.jwt.TokenProvider;
import com.skklub.admin.security.redis.RedisUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Objects;
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RefreshTokenService {
    private final PrincipalDetailsService principalDetailsService;
    private final RedisUtil redisUtil;

    //access-token 재발급
    public String refreshAccessToken(HttpServletRequest request, Long userId, String username, Role role){
        validateRefreshToken(request, username);
        String newAccessToken = TokenProvider.createAccessJwt(userId,username,role);
        log.info("access-token reissued");
        return newAccessToken;
    }

    //refresh-token 검증
    public void validateRefreshToken(HttpServletRequest request, String username) throws InvalidTokenException {
        String refreshToken = request.getHeader("Refresh-Token");
        //refresh-token 존재여부 확인
        if (Objects.isNull(refreshToken)) {
            throw new InvalidTokenException(ErrorCode.TOKEN_NOT_FOUND, "refresh-token not found");
        }
        //refresh-token "Bearer " 접두사 확인
        if (!refreshToken.startsWith("Bearer ")) {
            log.error("invalid token : wrong token prefix");
            throw new InvalidTokenException(ErrorCode.INVALID_TOKEN, "invalid refresh-token");
        }
        //토큰 파싱
        String resolvedRefreshToken = TokenProvider.resolveToken(refreshToken);
        UserDetails userDetails = principalDetailsService.loadUserByUsername(username);

        //redis 서버 내 refresh-token 존재여부 확인
        String key = "RT:"+userDetails.getUsername();
        if(!redisUtil.hasKeyRefreshToken(key)){
            throw new InvalidTokenException(ErrorCode.TOKEN_NOT_FOUND, "refresh-token not found");
        }
        //redis 서버 내 refresh-token & request 로 받은 토큰 동일 여부 확인
        if (!redisUtil.getRefreshToken(key).equals(resolvedRefreshToken)) {
            log.info("invalid refresh-token : doesn't match with token in server");
            throw new InvalidTokenException(ErrorCode.INVALID_TOKEN, "invalid token");
        }
    }

}