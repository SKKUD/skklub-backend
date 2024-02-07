package com.skklub.admin.controller;

import com.skklub.admin.controller.dto.ClubCreateResponse;
import com.skklub.admin.controller.dto.PendingClubRequest;
import com.skklub.admin.controller.dto.PendingInformationResponse;
import com.skklub.admin.domain.Club;
import com.skklub.admin.domain.PendingClub;
import com.skklub.admin.domain.User;
import com.skklub.admin.domain.enums.Campus;
import com.skklub.admin.domain.enums.ClubType;
import com.skklub.admin.domain.enums.Role;
import com.skklub.admin.exception.deprecated.error.exception.CannotRequestCreationToUserException;
import com.skklub.admin.exception.deprecated.error.exception.InvalidApproachException;
import com.skklub.admin.exception.deprecated.error.exception.PendingClubIdMisMatchException;
import com.skklub.admin.exception.deprecated.error.handler.ClubValidator;
import com.skklub.admin.repository.PendingClubRepository;
import com.skklub.admin.repository.UserRepository;
import com.skklub.admin.service.PendingClubService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PendingClubController {

    private final PendingClubService pendingClubService;
    private final PendingClubRepository pendingClubRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AuthValidator authValidator;

    //동아리 생성 요청
    @PostMapping("/pending")
    public PendingInformationResponse createPending(@ModelAttribute @Valid PendingClubRequest pendingClubRequest) {
        if(pendingClubRequest.getRequestTo().equals(Role.ROLE_USER)) throw new CannotRequestCreationToUserException();
        String encodedPw = bCryptPasswordEncoder.encode(pendingClubRequest.getPassword());
        PendingClub pendingClub = pendingClubService.requestCreation(pendingClubRequest.toEntity(encodedPw));
        return new PendingInformationResponse(pendingClub);
    }

    //생성 요청 조회
    @GetMapping("/pending")
    public Page<PendingInformationResponse> getPendings(@AuthenticationPrincipal UserDetails userDetails, Pageable pageable) {
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort().and(Sort.by("clubName").ascending()));
        String userName = userDetails.getUsername();
        User admin = userRepository.findByUsername(userName);
        if(admin.getRole().equals(Role.ROLE_USER)) throw new InvalidApproachException();
        log.info("admin.getRole() : {}", admin.getRole());
        return pendingClubRepository.findAllByRequestTo(admin.getRole(), pageRequest)
                .map(PendingInformationResponse::new);
    }

    //요청 승낙
    @DeleteMapping("/pending/{pendingClubId}/accept")
    public ClubCreateResponse acceptPending(@PathVariable Long pendingClubId, @RequestParam Campus campus, @RequestParam ClubType clubType, @RequestParam String belongs) {
        authValidator.validatePendingRequestAuthority(pendingClubId);
        ClubValidator.validateBelongs(campus, clubType, belongs);
        Club createdClub = pendingClubService.acceptRequest(pendingClubId, campus, clubType, belongs)
                .orElseThrow(PendingClubIdMisMatchException::new);
        return new ClubCreateResponse(createdClub);
    }


    //요청 거절
    @DeleteMapping("/pending/{pendingClubId}/deny")
    public PendingInformationResponse denyPending(@PathVariable Long pendingClubId) {
        authValidator.validatePendingRequestAuthority(pendingClubId);
        PendingClub pendingClub = pendingClubService.denyRequest(pendingClubId)
                .orElseThrow(PendingClubIdMisMatchException::new);
        return new PendingInformationResponse(pendingClub);
    }
}
