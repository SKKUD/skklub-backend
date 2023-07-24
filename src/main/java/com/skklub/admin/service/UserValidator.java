package com.skklub.admin.service;

import com.skklub.admin.domain.User;
import com.skklub.admin.domain.enums.Role;
import com.skklub.admin.error.exception.PendingClubIdMisMatchException;
import com.skklub.admin.exception.AuthException;
import com.skklub.admin.exception.ErrorCode;
import com.skklub.admin.repository.*;
import com.skklub.admin.service.enums.ValidatingTypes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserValidator {

    private final UserRepository userRepository;
    private final ClubRepository clubRepository;
    private final NoticeRepository noticeRepository;
    //private final RecruitRepository recruitRepository;
    private final PendingClubRepository pendingClubRepository;

    public void validateUsernameDuplication(String username){
        Optional.ofNullable(userRepository.findByUsername(username))
                .ifPresent(user->{
                    throw new AuthException(ErrorCode.USERNAME_DUPLICATED, username+" is already exists");
                });
    }

    public void validateUpdate(ValidatingTypes validatingTypes, Long targetId){
        Optional<UserDetails> nowUserDetails = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getPrincipal)
                .filter(principal -> principal instanceof UserDetails)
                .map(principal -> (UserDetails) principal);

        if (nowUserDetails.isPresent()) {
            UserDetails userDetails = nowUserDetails.get();
            switchValidation(validatingTypes,userDetails,targetId);
        }else{
            throw new AuthException(ErrorCode.NO_AUTHORIZED_USER,"no authorized user");
        }

    }

    private void switchValidation(ValidatingTypes validatingTypes, UserDetails userDetails, Long targetId) {
        switch(validatingTypes) {
            case USER:
                validateUpdatingUser(userDetails,targetId);
                break;
            case CLUB:
                validateUpdatingClub(userDetails,targetId);
                break;
            case NOTICE:
                validateUpdatingNotice(userDetails,targetId);
                break;
            case RECRUIT:
                validateUpdatingRecruit(userDetails, targetId);
                break;
            case PENDING:
                validatePendingRequestAuthority(userDetails,targetId);
                break;
            default:
                throw new RuntimeException("invalid validation type");
        }
    }

    private void validateUpdatingUser(UserDetails nowUser, Long updatedUserId) {
        //수정 권한자로 등록된 유저 확인
        User registeredUser = Optional.of(userRepository.findById(updatedUserId).get())
                .orElseThrow(() ->
                        new AuthException(ErrorCode.USER_NOT_FOUND, "no existing user"));
        //업데이트 대상 계정과 로그인된 계정 일치 여부 확인
        if (!nowUser.getUsername().equals(registeredUser.getUsername())) {
            //관리자에 의한 직권 수정 허용(MASTER, ADMIN)
            String nowUserAuthority = getUserAuthority(nowUser);
            if (nowUserAuthority.equals(String.valueOf(Role.ROLE_ADMIN_SEOUL_CENTRAL)) || nowUserAuthority.equals(String.valueOf(Role.ROLE_ADMIN_SUWON_CENTRAL)) ||  nowUserAuthority.equals(String.valueOf(Role.ROLE_MASTER))) {
                log.info("now updating with authority of administrator({}): {}", nowUserAuthority, nowUser.getUsername());
            } else {
                throw new AuthException(ErrorCode.NO_AUTHORITY, "no authority");
            }
        }
    }

    private void validateUpdatingClub(UserDetails nowUser, Long clubId) {
        User registeredUser = Optional.of(clubRepository.findById(clubId).get().getPresident())
                .orElseThrow(() ->
                        new AuthException(ErrorCode.USER_NOT_FOUND, "no existing user"));
       validateUpdatingUser(nowUser,registeredUser.getId());
    }

    private void validateUpdatingNotice(UserDetails nowUser, Long noticeId) {
        User registeredUser = Optional.of(noticeRepository.findById(noticeId).get().getWriter())
                .orElseThrow(() ->
                        new AuthException(ErrorCode.USER_NOT_FOUND, "no existing user"));
        validateUpdatingUser(nowUser,registeredUser.getId());
    }

    private void validateUpdatingRecruit(UserDetails nowUser, Long id) {
        //???
    }

    public void validatePendingRequestAuthority(UserDetails nowUser, Long pendingClubId) {
        Role requestTo = Optional.of(pendingClubRepository.findById(pendingClubId).get().getRequestTo())
                .orElseThrow(PendingClubIdMisMatchException::new);
        log.warn("requested-to-authority: {} now authority: {}", requestTo, getUserAuthority(nowUser));
        if (!getUserAuthority(nowUser).equals(String.valueOf(requestTo))) {
            log.warn("requested-to-authority: {} now authority: {}", requestTo, getUserAuthority(nowUser));
            throw new AuthException(ErrorCode.WRONG_REQUEST, "no authority to accept or deny pending request");
        }
    }

    private String getUserAuthority(UserDetails nowUser){
        List<GrantedAuthority> authList = (List<GrantedAuthority>) nowUser.getAuthorities();
        return authList.get(0).getAuthority();
    }

    //    public void validateUpdatingAuthority(UserDetails nowUser, Role requiredAuthority) {
//        if (!getUserAuthority(nowUser).equals(String.valueOf(requiredAuthority))) {
//            log.warn("required authority: {} now authority: {}", requiredAuthority, getUserAuthority(nowUser));
//            throw new AuthException(ErrorCode.NO_AUTHORITY, "no authority to update");
//        }
//    }

}
