package com.skklub.admin.controller;

import com.skklub.admin.controller.dto.*;
import com.skklub.admin.domain.enums.Role;
import com.skklub.admin.security.jwt.TokenProvider;
import com.skklub.admin.security.jwt.dto.JwtDTO;
import com.skklub.admin.service.UserService;
import com.skklub.admin.service.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import static org.apache.http.HttpHeaders.AUTHORIZATION;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private static final Role role = Role.valueOf("ROLE_USER");

    //join
    @PostMapping(value = "/user/join")
    public ResponseEntity<UserProcResultDTO> join(@ModelAttribute UserJoinRequestDTO userJoinRequestDTO) {
        log.info("username : {}, password : {}", userJoinRequestDTO.getUsername(), userJoinRequestDTO.getPassword());
        UserProcResultDTO joined = userService.userJoin(new UserJoinDTO(userJoinRequestDTO.getUsername(), userJoinRequestDTO.getPassword(), role, userJoinRequestDTO.getName(), userJoinRequestDTO.getContact()));
        return ResponseEntity.ok().body(joined);
    }

    //login
    @PostMapping(value = "/user/login")
    public ResponseEntity<String> login(@ModelAttribute UserLoginRequestDto userLoginRequestDto) {
        log.info("username : {}, password : {}", userLoginRequestDto.getUsername(), userLoginRequestDto.getPassword());
        JwtDTO tokens = userService.userLogin(new UserLoginDTO(userLoginRequestDto.getUsername(), userLoginRequestDto.getPassword()));

        HttpHeaders headers = new HttpHeaders();
        headers.set(AUTHORIZATION,"Bearer " + tokens.getAccessToken());
        headers.set("Refresh-Token","Bearer " + tokens.getRefreshToken());
        String msg = userLoginRequestDto.getUsername() + " logged in";
        log.info(msg);

        return new ResponseEntity<>(msg, headers, HttpStatus.valueOf(200));
    }

    //update
    @PostMapping(value = "/user/{userId}")
    public ResponseEntity<UserProcResultDTO> update(@PathVariable Long userId, @ModelAttribute UserUpdateRequestDTO userUpdateRequestDTO, @AuthenticationPrincipal UserDetails userDetails){
        UserProcResultDTO updated = userService.userUpdate(
                new UserUpdateDTO(
                        userId, userUpdateRequestDTO.getPassword(), role, userUpdateRequestDTO.getName(),userUpdateRequestDTO.getContact(),userDetails));
        return ResponseEntity.ok().body(updated);
    }

    //logout
    @PostMapping("/user/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        String username = userService.userLogout(
                new UserLogoutDTO(
                        request.getHeader(HttpHeaders.AUTHORIZATION),
                        TokenProvider.getAuthentication(userDetails).getName())
        );
        String msg = username+ " logged out successfully";
        log.info(msg);
        return ResponseEntity.ok(msg);
    }

}
