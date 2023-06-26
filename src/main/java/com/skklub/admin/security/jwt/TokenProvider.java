package com.skklub.admin.security.jwt;

import com.skklub.admin.exception.AuthException;
import com.skklub.admin.exception.ErrorCode;
import com.skklub.admin.exception.InvalidTokenException;
import com.skklub.admin.security.jwt.dto.JwtDTO;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Objects;

@Slf4j
@Component
public class TokenProvider {
    private final String secret;
    private static  Key secretKey;
    //access-token : 30 min
    public static final int accessTokenExpiredMs = 60 * 30;
    //refresh-token : 7 days(1 week)
    public static final int refreshTokenExpiredMs = 60 * 60 * 24 * 7;

    public TokenProvider(@Value("${jwt.secret}") String secret) {
        this.secret = secret;
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public static JwtDTO createTokens(String username) {
        //access-token 발급
        String accessToken = createAccessJwt(username);
        //refresh-token 발급
        String refreshToken = createRefreshJwt();

        return new JwtDTO(accessToken,refreshToken);
    }

    public static String createAccessJwt(String username){
        Claims claims = Jwts.claims();
        claims.put("username",username);

        return Jwts.builder()
                .setSubject("access-token")
                .setClaims(claims)
                .setIssuer("SKKU.D")
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+accessTokenExpiredMs * 1000))
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }

    public static String createRefreshJwt(){

        return Jwts.builder()
                .setSubject("refresh-token")
                .setIssuer("SKKU.D")
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+refreshTokenExpiredMs * 1000))
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }

    public static String resolveToken(String token){
        return token.split(" ")[ 1];
    }

    public static Long getExpiration(String token) {
        Date expiration = Jwts.parserBuilder().setSigningKey(secretKey)
                .build().parseClaimsJws(token).getBody().getExpiration();
        long now = new Date().getTime();
        return expiration.getTime() - now;
    }

    public static String getUsername(String token){
        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token)
                .getBody().get("username", String.class);
    }

    public static UsernamePasswordAuthenticationToken getAuthentication(UserDetails userDetails) {
        return new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities());
    }

}
