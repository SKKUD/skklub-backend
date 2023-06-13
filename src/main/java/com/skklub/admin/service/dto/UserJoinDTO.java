package com.skklub.admin.service.dto;

import com.skklub.admin.domain.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserJoinDTO {
    private Long id;
    private String username;
    private String password;
    private Role role;
    private String name;
    private String contact;

    public UserJoinDTO(String username, String password, Role role, String name, String contact){
        this.username = username;
        this.password = password;
        this.role = role;
        this.name = name;
        this.contact = contact;
    }
}
