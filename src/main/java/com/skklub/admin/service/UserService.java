package com.skklub.admin.service;

import com.skklub.admin.domain.User;
import com.skklub.admin.domain.enums.Role;
import com.skklub.admin.exception.AuthException;
import com.skklub.admin.exception.ErrorCode;
import com.skklub.admin.repository.UserRepository;
import com.skklub.admin.security.jwt.TokenProvider;
import com.skklub.admin.security.jwt.dto.JwtDTO;
import com.skklub.admin.security.redis.RedisUtil;
import com.skklub.admin.service.dto.UserJoinDTO;
import com.skklub.admin.service.dto.UserLoginDTO;
import com.skklub.admin.service.dto.UserProcResultDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final TokenProvider tokenProvider;
    private final RedisUtil redisUtil;

    //User Join
    public UserProcResultDTO joinUser(UserJoinDTO userJoinDTO){
        //username 중복 검사
        usernameDuplicationValidate(userJoinDTO.getUsername());
        //회원가입 진행
        String encPwd = bCryptPasswordEncoder.encode(userJoinDTO.getPassword());
        User user =
                new User(userJoinDTO.getUsername(),
                        encPwd,
                        userJoinDTO.getRole(),
                        userJoinDTO.getName(),
                        userJoinDTO.getContact());
        userRepository.save(user);
        UserProcResultDTO result = new UserProcResultDTO(user.getId(),user.getUsername(),user.getName(),user.getContact());
        log.info("new user joined -> userId : {}, username : {}", user.getId(), user.getUsername());
        log.info("new user joined -> userPW : {}", user.getPassword());
        return result;
    }

    public void usernameDuplicationValidate(String username){
        Optional.ofNullable(userRepository.findByUsername(username))
                .ifPresent(user->{
                    throw new AuthException(ErrorCode.USERNAME_DUPLICATED, username+" is already exists");
                });
    }

    //User Login
    public JwtDTO loginUser(UserLoginDTO userLoginDTO){
        String username = userLoginDTO.getUsername();
        String password = userLoginDTO.getPassword();
        //기본 username, password 인증
        //1. 일치하는 username 없음
        User selectedUser = Optional.ofNullable(userRepository.findByUsername(username))
                .orElseThrow(()->
                    new AuthException(ErrorCode.USER_NOT_FOUND, "invalid user account"));
        //2. password 불일치
        if(!bCryptPasswordEncoder.matches(password,selectedUser.getPassword())){
            throw new AuthException(ErrorCode.USER_NOT_FOUND, "invalid user account");
        }
        //토큰 발급
        JwtDTO tokens = tokenProvider.createTokens(username);
        String refreshToken = tokens.getRefreshToken();
        String key = "RT:" + username;
        //Redis 저장된 refreshToken 확인 -> 업데이트
        if(redisUtil.hasKeyRefreshToken(key)) {
            redisUtil.deleteRefreshToken(key);
        }redisUtil.setRefreshToken(key, refreshToken, TokenProvider.getExpiration(refreshToken), TimeUnit.MILLISECONDS);
        //정상 로그인 완료 -> JWT 반환
        return tokens;
    }

    //User Update
    public Optional<User> updateUser(Long userId, String password, Role role, String name, String contact, UserDetails userDetails, String accessToken){

        validateUserAuthority(userDetails,userId);
        //update 진행
        String username = userRepository.findById(userId).get().getUsername();
        String encPwd = bCryptPasswordEncoder.encode(password);

        return userRepository.findById(userId)
                .map(baseUser -> {
                    baseUser.update(new User(username, encPwd, role, name, contact));
                    invalidateUserAccessToken(username, accessToken);
                    log.info("user updated -> userId : {}, username : {}", userId, username);
                    return baseUser;
                });
    }

    //User Logout
    public String logoutUser(String username,String accessToken) {
        invalidateUserAccessToken(username, accessToken);
        return username;
    }

    public void validateUserAuthority(UserDetails nowUser, Long id){
        //수정 권한자로 등록된 유저 확인
        User registeredUser = Optional.of(userRepository.findById(id).get())
                .orElseThrow(()->
                        new AuthException(ErrorCode.USER_NOT_FOUND, "no existing user"));
        //업데이트 대상 계정과 로그인된 계정 일치 여부 확인
        if(!nowUser.getUsername().equals(registeredUser.getUsername())){
            //관리자에 의한 직권 수정 허용(MASTER, ADMIN)
            List<GrantedAuthority> authList = (List<GrantedAuthority>) nowUser.getAuthorities();
            String authority = authList.get(0).getAuthority();
            if(authority.equals(String.valueOf(Role.ROLE_ADMIN_SEOUL_CENTRAL))||authority.equals(String.valueOf(Role.ROLE_MASTER))){
                log.info("now updating with authority of administrator({}): {}",authority, nowUser.getUsername());
            }else{
                throw new AuthException(ErrorCode.NO_AUTHORITY,"no authority");
            }
        }
    }

    private void invalidateUserAccessToken(String username, String accessToken) {
        //Redis 에 저장된 refresh token 있을 경우 삭제
        if (redisUtil.hasKeyRefreshToken("RT:" + username)) {
            redisUtil.deleteRefreshToken("RT:" + username);
        }
        //해당 AccessToken 유효시간 반영해서 BlackList 저장
        String bannedToken = TokenProvider.resolveToken(accessToken);
        redisUtil.setBlackList(bannedToken, "AT:" + username, TokenProvider.getExpiration(bannedToken) + (60 * 10 * 1000), TimeUnit.MILLISECONDS);
    }

}
