
package com.skklub.admin.controller;

import com.skklub.admin.domain.Club;
import com.skklub.admin.domain.DeletedClub;
import com.skklub.admin.domain.Notice;
import com.skklub.admin.domain.User;
import com.skklub.admin.domain.enums.Role;
import com.skklub.admin.error.exception.ClubIdMisMatchException;
import com.skklub.admin.error.exception.NoticeIdMisMatchException;
import com.skklub.admin.error.exception.PendingClubIdMisMatchException;
import com.skklub.admin.exception.AuthException;
import com.skklub.admin.exception.ErrorCode;
import com.skklub.admin.repository.*;
import com.skklub.admin.security.auth.PrincipalDetailsService;
import jakarta.annotation.PostConstruct;
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
    private final DeletedClubRepository deletedClubRepository;

    public void validateUpdatingUser(Long userId) throws AuthException{
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
                throw new AuthException(ErrorCode.INVALID_AUTHORITY, "no authority");
            }
        }
    }

    public void validateUpdatingClub(Long clubId) throws AuthException{
        Club club = clubRepository.findById(clubId)
                .orElseThrow(ClubIdMisMatchException::new);
        User user = club.getPresident();
        validateUpdatingUser(user.getId());
    }

    public void validateDeletionAuth(Long deletedClubId) throws AuthException{
        DeletedClub club = deletedClubRepository.findById(deletedClubId)
                .orElseThrow(ClubIdMisMatchException::new);
        User user = userRepository.findById(club.getUserId()).orElseThrow(() -> new AuthException(ErrorCode.USER_NOT_FOUND, "no existing user"));
        validateUpdatingUser(user.getId());
    }

    public void validateUpdatingNotice(Long noticeId) throws AuthException{
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(NoticeIdMisMatchException::new);
        User writer = notice.getWriter();
        validateUpdatingUser(writer.getId());
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
            throw new AuthException(ErrorCode.NO_AUTHORIZED_USER,"no userdetails in securityContext");
        }
        return principalDetailsService.loadUserByUsername(
                SecurityContextHolder.getContext().getAuthentication().getName()
        );
    }

    private String getUserAuthority(UserDetails userDetails) throws AuthException{
        List<GrantedAuthority> authList = (List<GrantedAuthority>) userDetails.getAuthorities();
        if(authList.size()==0){
            throw new AuthException(ErrorCode.NO_AUTHORITY,"no granted authorities for user");
        }
        return authList.get(0).getAuthority();
    }

}
