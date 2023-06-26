package com.skklub.admin.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserJoinRequestDTO {
    private String username;
    private String password;
    private String name;
    private String contact;
    
}
