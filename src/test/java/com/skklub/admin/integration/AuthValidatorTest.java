package com.skklub.admin.integration;

import com.skklub.admin.TestDataRepository;
import com.skklub.admin.controller.AuthValidator;
import com.skklub.admin.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional
@Import(TestDataRepository.class)
@ExtendWith(MockitoExtension.class)
@SpringBootTest
public class AuthValidatorTest {
    @Autowired
    private AuthValidator authValidator;
    @Autowired
    private UserRepository userRepository;

    @Before
    public void setUser() {

    }
//
//    @Test
//    @WithMockCustomUser(username = "userId0", password = "userPw0")
//    public void validateUser_Default_Failed() throws Exception {
//        //given
//        System.out.println(SecurityContextHolder.getContext().getAuthentication().getName());
//        System.out.println(userRepository.findByUsername("userId0").getUsername());
//        System.out.println(userRepository.findByUsername("userId1").getUsername());
//        User wrongUser = userRepository.findByUsername("userId1");
//        //when, then
//        Throwable exception = assertThrows(AuthException.class, () -> {
//            authValidator.validateUpdatingUser(wrongUser.getId());
//        });
//        Assertions.assertThat(exception.getMessage()).isEqualTo("no authority");
//    }


}
