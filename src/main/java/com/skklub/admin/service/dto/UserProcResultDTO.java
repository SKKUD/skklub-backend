package com.skklub.admin.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProcResultDTO {
    private Long id;
    private String username;
    private String name;
    private String contact;
}
