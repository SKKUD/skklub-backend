package com.skklub.admin.service.dto;

import com.skklub.admin.domain.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateDTO {
    private Long id;
    private String password;
    private Role role;
    private String name;
    private String contact;
    private UserDetails userDetails;
}
