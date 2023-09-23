package com.skklub.admin;

import com.skklub.admin.domain.User;
import com.skklub.admin.domain.enums.Role;
import com.skklub.admin.repository.UserRepository;
import com.skklub.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TestUserJoin {
    private final UserRepository userRepository;
    private final UserService userService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    //User Join deprecated
    public Long joinUser(String username, String password, Role role, String name, String contact){
        //username 중복 검사
        userService.validateUsernameDuplication(username);
        //회원가입 진행
        String encPwd = bCryptPasswordEncoder.encode(password);
        User user = new User(username, encPwd, role, name, contact);
        userRepository.save(user);

        return userRepository.findByUsername(username).getId();
    }
}

