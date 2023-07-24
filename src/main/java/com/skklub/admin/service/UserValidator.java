package com.skklub.admin.service;

import com.skklub.admin.domain.User;
import com.skklub.admin.domain.enums.Role;
import com.skklub.admin.exception.AuthException;
import com.skklub.admin.exception.ErrorCode;
import com.skklub.admin.repository.ClubRepository;
import com.skklub.admin.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserValidator {
    private final UserRepository userRepository;
    private final ClubRepository clubRepository;

    public void usernameDuplicationValidate(String username){
        Optional.ofNullable(userRepository.findByUsername(username))
                .ifPresent(user->{
                    throw new AuthException(ErrorCode.USERNAME_DUPLICATED, username+" is already exists");
                });
    }

    public void validateUpdatingUser(UserDetails nowUser, Long id) {
        //수정 권한자로 등록된 유저 확인
        User registeredUser = Optional.of(userRepository.findById(id).get())
                .orElseThrow(() ->
                        new AuthException(ErrorCode.USER_NOT_FOUND, "no existing user"));
        //업데이트 대상 계정과 로그인된 계정 일치 여부 확인
        if (!nowUser.getUsername().equals(registeredUser.getUsername())) {
            //관리자에 의한 직권 수정 허용(MASTER, ADMIN)
            List<GrantedAuthority> authList = (List<GrantedAuthority>) nowUser.getAuthorities();
            String authority = authList.get(0).getAuthority();
            if (authority.equals(String.valueOf(Role.ROLE_ADMIN_SEOUL_CENTRAL)) || authority.equals(String.valueOf(Role.ROLE_MASTER))) {
                log.info("now updating with authority of administrator({}): {}", authority, nowUser.getUsername());
            } else {
                throw new AuthException(ErrorCode.NO_AUTHORITY, "no authority");
            }
        }
    }

    public void validateAuthority(UserDetails nowUser, Long id) {

    }

}
