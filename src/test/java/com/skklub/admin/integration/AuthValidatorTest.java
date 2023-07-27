package com.skklub.admin.integration;

import com.skklub.admin.TestDataRepository;
import com.skklub.admin.WithMockCustomUser;
import com.skklub.admin.WithMockCustomUserSecurityContextFactory;
import com.skklub.admin.controller.AuthValidator;
import com.skklub.admin.controller.UserController;
import com.skklub.admin.domain.User;
import com.skklub.admin.exception.AuthException;
import com.skklub.admin.repository.ClubRepository;
import com.skklub.admin.repository.NoticeRepository;
import com.skklub.admin.repository.PendingClubRepository;
import com.skklub.admin.repository.UserRepository;
import com.skklub.admin.security.auth.PrincipalDetailsService;
import com.skklub.admin.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
@Transactional
@Import(TestDataRepository.class)
@ExtendWith(MockitoExtension.class)
@SpringBootTest
public class AuthValidatorTest {

    @InjectMocks
    private TestDataRepository testDataRepository;
    @Mock
    private AuthValidator authValidator;
    @Mock
    private ClubRepository clubRepository;
    @Autowired
    private UserRepository userRepository;
    @Mock
    private PendingClubRepository pendingClubRepository;
    @Mock
    private NoticeRepository noticeRepository;
    @Mock
    private UserService userService;
    @Mock
    private UserController userController;
    @Mock
    private PrincipalDetailsService principalDetailsService;
    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Mock
    private WithMockCustomUserSecurityContextFactory securityContextFactory;

    @Before
    public void setUser() {

    }

    @Test
    @WithMockCustomUser(username = "userId0", password = "userPw0")
    public void validateUser_Default_Failed() throws Exception {
        //given
        System.out.println(SecurityContextHolder.getContext().getAuthentication().getName());
        System.out.println(userRepository.findByUsername("userId0").getUsername());
        System.out.println(userRepository.findByUsername("userId1").getUsername());
        User wrongUser = userRepository.findByUsername("userId1");
        //when, then
        Throwable exception = assertThrows(AuthException.class, () -> {
            authValidator.validateUpdatingUser(wrongUser.getId());
        });
        Assertions.assertThat(exception.getMessage()).isEqualTo("no authority");
    }


//    @Test
//    @WithMockCustomUser(username = "userId0", password = "userPw0")
//    public void validateClub_Default_Failed() throws Exception {
//        //given
//        Club wrongClub = clubRepository.findById("userId1");
//        //when, then
//        Throwable exception = assertThrows(AuthException.class, () -> {
//            authValidator.validateUpdatingUser(wrongUser.getId());
//        });
//        Assertions.assertThat(exception.getMessage()).isEqualTo("no authority");
//    }

}
