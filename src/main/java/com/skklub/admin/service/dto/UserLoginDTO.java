package com.skklub.admin.service.dto;

import com.skklub.admin.domain.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginDTO {
    private Long id;
    private String username;
    private Role role;
    private String accessToken;
    private String refreshToken;
}
