package com.skklub.admin.deprecated.service;

import com.skklub.admin.TestUserJoin;
import com.skklub.admin.domain.enums.Role;
import com.skklub.admin.security.redis.RedisUtil;
import com.skklub.admin.service.RefreshTokenService;
import com.skklub.admin.service.UserService;
import com.skklub.admin.service.dto.UserLoginDTO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;

import java.security.Key;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@SpringBootTest
@Transactional
public class RefreshTokenServiceTest {
    private final UserService userService;

    private final TestUserJoin testUserJoin;

    private final RefreshTokenService refreshTokenService;

    private final RedisUtil redisUtil;

    private final String secret;
    private final  Key secretKey;

    @Autowired
    public RefreshTokenServiceTest(UserService userService,
                                   TestUserJoin testUserJoin, RedisUtil redisUtil,
                                   RefreshTokenService refreshTokenService,
                                   @Value("${jwt.secret}") String secret
    ){
        this.userService = userService;
        this.testUserJoin = testUserJoin;
        this.redisUtil = redisUtil;
        this.refreshTokenService = refreshTokenService;
        this.secret = secret;
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    private boolean validateToken(String token, Key secretKey){

        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
            return true;
        } catch (SignatureException | MalformedJwtException e) {
            return false;
        } catch (ExpiredJwtException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Test
    public void RefreshToken_Success(){
        //given
        String username = "user";
        String password = "1234";
        Role role = Role.ROLE_USER;
        String name = "명륜이";
        String contact = "010-1234-5678";

        Long userId = testUserJoin.joinUser(username, password, role, name, contact);

        UserLoginDTO jwtDTO = userService.loginUser(username,password);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("refresh-token","Bearer "+jwtDTO.getRefreshToken());
        //when

        String newAccessToken = refreshTokenService.refreshAccessToken(request,userId,username,role);

        //then
        assertTrue(validateToken(newAccessToken,secretKey));
    }

}
