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
import com.skklub.admin.error.exception.CannotRequestCreationToUserException;
import com.skklub.admin.error.exception.InvalidApproachException;
import com.skklub.admin.error.exception.PendingClubIdMisMatchException;
import com.skklub.admin.error.handler.ClubValidator;
import com.skklub.admin.repository.PendingClubRepository;
import com.skklub.admin.repository.UserRepository;
import com.skklub.admin.security.jwt.TokenProvider;
import com.skklub.admin.service.PendingClubService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PendingClubController {

    private final PendingClubService pendingClubService;
    private final PendingClubRepository pendingClubRepository;
    private final UserRepository userRepository;

    //동아리 생성 요청
    @PostMapping("/pending")
    public PendingInformationResponse createPending(@ModelAttribute @Valid PendingClubRequest pendingClubRequest) {
        if(pendingClubRequest.getRequestTo().equals(Role.ROLE_USER)) throw new CannotRequestCreationToUserException();
        PendingClub pendingClub = pendingClubService.requestCreation(pendingClubRequest.toEntity());
        return new PendingInformationResponse(pendingClub);
    }

    //생성 요청 조회
    @GetMapping("/pending")
    public Page<PendingInformationResponse> getPendingList(@AuthenticationPrincipal UserDetails userDetails, Pageable pageable) {
        String userName = TokenProvider.getAuthentication(userDetails).getName();
        User admin = userRepository.findByUsername(userName);
        if(admin.getRole().equals(Role.ROLE_USER)) throw new InvalidApproachException();
        return pendingClubRepository.findAllByRequestTo(admin.getRole(), pageable)
                .map(PendingInformationResponse::new);
    }

    //요청 승낙
    @PostMapping("/pending/{pendingClubId}/accept")
    public ClubCreateResponse acceptPending(@PathVariable Long pendingClubId, @RequestParam Campus campus, @RequestParam ClubType clubType, @RequestParam String belongs) {
        ClubValidator.validateBelongs(campus, clubType, belongs);
        Club createdClub = pendingClubService.acceptRequest(pendingClubId, campus, clubType, belongs)
                .orElseThrow(PendingClubIdMisMatchException::new);
        return new ClubCreateResponse(createdClub);
    }


    //요청 거절
    @PostMapping("/pending/{pendingClubId}/deny")
    public PendingInformationResponse denyPending(@PathVariable Long pendingClubId) {
        PendingClub pendingClub = pendingClubService.denyRequest(pendingClubId)
                .orElseThrow(PendingClubIdMisMatchException::new);
        return new PendingInformationResponse(pendingClub);
    }
}
