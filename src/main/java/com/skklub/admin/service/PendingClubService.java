package com.skklub.admin.service;

import com.skklub.admin.domain.Club;
import com.skklub.admin.domain.PendingClub;
import com.skklub.admin.domain.User;
import com.skklub.admin.domain.enums.Campus;
import com.skklub.admin.domain.enums.ClubType;
import com.skklub.admin.repository.ClubRepository;
import com.skklub.admin.repository.PendingClubRepository;
import com.skklub.admin.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PendingClubService {

    private final PendingClubRepository pendingClubRepository;
    private final ClubRepository clubRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public PendingClub requestCreation(PendingClub pendingClub) {
        pendingClubRepository.save(pendingClub);
        return pendingClub;
    }

    public Optional<Club> acceptRequest(Long pendingClubId, Campus campus, ClubType clubType, String belongs) {
        return pendingClubRepository.findById(pendingClubId)
                .map(
                        pendingClub -> {
                            User user = pendingClub.toUser();
                            userService.validateUsernameDuplication(user.getUsername());
                            userRepository.save(user);
                            Club club = pendingClub.toClubWithDefaultLogo(campus, clubType, belongs, user);
                            clubRepository.save(club);
                            pendingClubRepository.delete(pendingClub);
                            return club;
                        }
                );
    }

    public Optional<PendingClub> denyRequest(Long pendingClubId) {
        return pendingClubRepository.findById(pendingClubId)
                .map(
                        pendingClub -> {
                            pendingClubRepository.delete(pendingClub);
                            return pendingClub;
                        }
                );
    }
}
