package com.skklub.admin.controller.dto;

import com.skklub.admin.domain.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginResponseDTO {
    private Long id;
    private String username;
    private Role role;
}
