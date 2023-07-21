package com.skklub.admin.service;

import com.skklub.admin.domain.User;
import com.skklub.admin.domain.enums.Role;
import com.skklub.admin.repository.UserRepository;
import com.skklub.admin.security.auth.PrincipalDetailsService;
import com.skklub.admin.security.jwt.TokenProvider;
import com.skklub.admin.security.jwt.dto.JwtDTO;
import com.skklub.admin.security.redis.RedisUtil;
import com.skklub.admin.service.dto.UserJoinDTO;
import com.skklub.admin.service.dto.UserLoginDTO;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@Transactional
public class UserServiceTest {
    private final UserService userService;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private final PrincipalDetailsService principalDetailsService;

    private final RedisUtil redisUtil;

    @Autowired
    public UserServiceTest(UserService userService, UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, PrincipalDetailsService principalDetailsService,RedisUtil redisUtil){
        this.userService = userService;
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.principalDetailsService = principalDetailsService;
        this.redisUtil = redisUtil;
    }

    @Test
    public void userJoin_Success(){
        //given
        String username = "user";
        String password = "1234";
        Role role = Role.ROLE_USER;
        String name = "명륜이";
        String contact = "010-1234-5678";

        UserJoinDTO userJoinDTO = new UserJoinDTO(username, password, role, name, contact);
        //when
        userService.joinUser(userJoinDTO);
        //then
        User user = userRepository.findByUsername(username);

        assertEquals(user.getUsername(),username);
        assertTrue(bCryptPasswordEncoder.matches(password,user.getPassword()));
        assertEquals(user.getRole(),role);
        assertEquals(user.getName(),name);
        assertEquals(user.getContact(), contact);
    }

    @Test
    public void userLogin_Success(){
        //given
        String username = "user";
        String password = "1234";
        Role role = Role.ROLE_USER;
        String name = "명륜이";
        String contact = "010-1234-5678";

        UserJoinDTO userJoinDTO = new UserJoinDTO(username, password, role, name, contact);
        userService.joinUser(userJoinDTO);

        UserLoginDTO userLoginDTO = new UserLoginDTO(username,password);
        //when
        JwtDTO jwtDTO = userService.loginUser(userLoginDTO);

        //then
        assertTrue(TokenProvider.getUsername(jwtDTO.getAccessToken()).equals(username));
        assertTrue(redisUtil.hasKeyRefreshToken("RT:" + username));
    }

    @Test
    public void userUpdate_Success(){
        //given
        String username = "tester";

        String password1 = "1234";
        Role role1 = Role.ROLE_USER;
        String name1 = "명륜이";
        String contact1 = "010-1234-5678";


        String password2 = "4321";
        Role role2 = Role.ROLE_USER;
        String name2 = "율전이";
        String contact2 = "010-8765-4321";

        UserJoinDTO userJoinDTO = new UserJoinDTO(username, password1, role1, name1, contact1);
        userService.joinUser(userJoinDTO);

        JwtDTO jwtDTO = userService.loginUser(new UserLoginDTO(username,password1));
        String accessToken = jwtDTO.getAccessToken();

        //when
        UserDetails userDetails = principalDetailsService.loadUserByUsername(username);
        User originalUser = userRepository.findByUsername(username);

        Long id = originalUser.getId();

        userService.updateUser(id, password2, role2, name2, contact2, userDetails,accessToken);

        //then
        User updatedUser = userRepository.findById(id).get();

        assertEquals(updatedUser.getId(),id);
        assertEquals(updatedUser.getUsername(),username);
        assertTrue(bCryptPasswordEncoder.matches(password2,updatedUser.getPassword()));
        assertEquals(updatedUser.getRole(),role2);
        assertEquals(updatedUser.getName(),name2);
        assertEquals(updatedUser.getContact(), contact2);
    }

    @Test
    public void userLogout_Success(){
        String username = "user";
        String password = "1234";
        Role role = Role.ROLE_USER;
        String name = "명륜이";
        String contact = "010-1234-5678";

        UserJoinDTO userJoinDTO = new UserJoinDTO(username, password, role, name, contact);
        userService.joinUser(userJoinDTO);

        JwtDTO jwtDTO = userService.loginUser(new UserLoginDTO(username,password));
        String accessToken = jwtDTO.getAccessToken();

        //when
        String loggedOutUser = userService.logoutUser(username,"Bearer "+accessToken);

        //then
        assertEquals(loggedOutUser, username);
        assertTrue(redisUtil.hasKeyBlackList(accessToken));
        assertFalse(redisUtil.hasKeyRefreshToken("RT:" + username));
        }



}
