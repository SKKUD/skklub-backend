package com.skklub.admin.service;

import com.skklub.admin.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class PendingClubServiceTest {
    @InjectMocks
    private PendingClubService pendingClubService;
    @Mock
    private PendingClubRepository pendingClubRepository;
    @Mock
    private ClubRepository clubRepository;
    @Mock
    private UserRepository userRepository;
}