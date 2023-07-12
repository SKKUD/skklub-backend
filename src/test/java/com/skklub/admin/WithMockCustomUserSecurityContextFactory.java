package com.skklub.admin;

import com.skklub.admin.domain.User;
import com.skklub.admin.security.auth.PrincipalDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;


public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser customUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        PrincipalDetails principalDetails = new PrincipalDetails(new User(customUser.username(), customUser.password(), customUser.role(), customUser.name(), customUser.contact()));
        Authentication authentication = new UsernamePasswordAuthenticationToken(principalDetails.getUsername(), principalDetails.getPassword(),principalDetails.getAuthorities());
        context.setAuthentication(authentication);
        return context;
    }

}
