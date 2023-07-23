package com.skklub.admin.controller.dto;

import com.skklub.admin.domain.PendingClub;
import com.skklub.admin.domain.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingClubRequest {
    @NotNull
    private Role requestTo;

    @NotBlank
    private String clubName;
    @NotBlank
    private String briefActivityDescription;
    @NotBlank
    private String activityDescription;
    @NotBlank
    private String clubDescription;

    @NotBlank
    private String username;
    @NotBlank
    private String password;
    @NotBlank
    private String presidentName;
    @NotBlank
    private String presidentContact;

    public PendingClub toEntity(String encodedPw) {
        return new PendingClub(
                this.clubName,
                this.briefActivityDescription,
                this.activityDescription,
                this.clubDescription,
                this.username,
                encodedPw,
                this.presidentName,
                this.presidentContact,
                this.requestTo
        );
    }
}
