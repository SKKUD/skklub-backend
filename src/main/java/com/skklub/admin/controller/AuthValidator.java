
package com.skklub.admin.controller;

import com.skklub.admin.domain.User;
import com.skklub.admin.domain.enums.Role;
import com.skklub.admin.error.exception.PendingClubIdMisMatchException;
import com.skklub.admin.exception.AuthException;
import com.skklub.admin.exception.ErrorCode;
import com.skklub.admin.repository.ClubRepository;
import com.skklub.admin.repository.NoticeRepository;
import com.skklub.admin.repository.PendingClubRepository;
import com.skklub.admin.repository.UserRepository;
import com.skklub.admin.security.auth.PrincipalDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthValidator {

    private final PrincipalDetailsService principalDetailsService;
    private final UserRepository userRepository;
    private final ClubRepository clubRepository;
    private final NoticeRepository noticeRepository;
    private final PendingClubRepository pendingClubRepository;

    public void validateUpdatingUser(Long userId) throws AuthException{
        System.out.println(userId);
        //수정 권한자로 등록된 유저 확인
        User registeredUser = Optional.of(userRepository.findById(userId).get())
                .orElseThrow(() ->
                        new AuthException(ErrorCode.USER_NOT_FOUND, "no existing user"));
        //업데이트 대상 계정과 로그인된 계정 일치 여부 확인
        if (!getUserDetails().getUsername().equals(registeredUser.getUsername())) {
            //관리자에 의한 직권 수정 허용(MASTER, ADMIN)
            String nowUserAuthority = getUserAuthority(getUserDetails());
            if (nowUserAuthority.equals(String.valueOf(Role.ROLE_ADMIN_SEOUL_CENTRAL)) || nowUserAuthority.equals(String.valueOf(Role.ROLE_ADMIN_SUWON_CENTRAL)) ||  nowUserAuthority.equals(String.valueOf(Role.ROLE_MASTER))) {
                log.info("now updating with authority of administrator({}): {}", nowUserAuthority, getUserDetails().getUsername());
            } else {
                throw new AuthException(ErrorCode.NO_AUTHORITY, "no authority");
            }
        }
    }

    public void validateUpdatingClub(Long clubId) throws AuthException{
        User registeredUser = Optional.of(clubRepository.findById(clubId).get().getPresident())
                .orElseThrow(() ->
                        new AuthException(ErrorCode.USER_NOT_FOUND, "no existing user"));
        validateUpdatingUser(registeredUser.getId());
    }

    public void validateUpdatingNotice(Long noticeId) throws AuthException{
        User registeredUser = Optional.of(noticeRepository.findById(noticeId).get().getWriter())
                .orElseThrow(() ->
                        new AuthException(ErrorCode.USER_NOT_FOUND, "no existing user"));
        validateUpdatingUser(registeredUser.getId());
    }

    public void validateUpdatingRecruit(Long recruitId) throws AuthException{
        Long PresidentId = Optional.of(clubRepository.findClubPresidentIdByRecruitId(recruitId).get(0))
                .orElseThrow(() ->
                        new AuthException(ErrorCode.USER_NOT_FOUND, "no existing user"));
        validateUpdatingUser(PresidentId);
    }

    public void validatePendingRequestAuthority(Long pendingClubId) throws PendingClubIdMisMatchException,AuthException{
        Role requestTo = Optional.of(pendingClubRepository.findById(pendingClubId).get().getRequestTo())
                .orElseThrow(PendingClubIdMisMatchException::new);
        log.warn("requested-to-authority: {} now authority: {}", requestTo, getUserAuthority(getUserDetails()));
        if (!getUserAuthority(getUserDetails()).equals(String.valueOf(requestTo))) {
            log.warn("requested-to-authority: {} now authority: {}", requestTo, getUserAuthority(getUserDetails()));
            throw new AuthException(ErrorCode.WRONG_REQUEST, "no authority to accept or deny pending request");
        }
    }

    private UserDetails getUserDetails() throws AuthException{
        if(SecurityContextHolder.getContext().getAuthentication()==null) {
            throw new AuthException(ErrorCode.NO_AUTHORIZED_USER,"no authorized user");
        }
        return principalDetailsService.loadUserByUsername(
                SecurityContextHolder.getContext().getAuthentication().getName()
        );
    }

    private String getUserAuthority(UserDetails userDetails){
        List<GrantedAuthority> authList = (List<GrantedAuthority>) userDetails.getAuthorities();
        return authList.get(0).getAuthority();
    }

}
