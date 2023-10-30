package com.skklub.admin;

import com.skklub.admin.domain.enums.Role;
import com.skklub.admin.service.UserService;
import com.skklub.admin.service.dto.UserLoginDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;

@SpringBootTest
@Transactional
public class JwtTokenTest {

    private final TestUserJoin testUserJoin;
    private final UserService userService;

    @Autowired
    public JwtTokenTest(TestUserJoin testUserJoin,UserService userService){
        this.userService = userService;
        this.testUserJoin  = testUserJoin;
    }

    @Test
    public void jwt_test() {
        //given
        String testUsername = "testUser";
        String testPw = "testPw";
        Role testRole = Role.ROLE_USER;
        String testName = "testName";
        String testContact = "testContact";
        Long testUserId = testUserJoin.joinUser(testUsername,testPw,testRole,testName,testContact);

        //when
        UserLoginDTO userLoginDTO = userService.loginUser(testUsername,testPw);
        //then
        String accessToken = userLoginDTO.getAccessToken();
        String[] split_string = accessToken.split("\\.");
        String base64EncodedHeader = split_string[0];
        String base64EncodedBody = split_string[1];
        String base64EncodedSignature = split_string[2];
        System.out.println(base64EncodedBody);
        Base64.Decoder decoder = Base64.getUrlDecoder();

        System.out.println(new String(decoder.decode(base64EncodedBody)));
    }
}
