package com.skklub.admin;

import com.skklub.admin.security.auth.PrincipalDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

@RequiredArgsConstructor
public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {
    private final PrincipalDetailsService principalDetailsService;

    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser customUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        UserDetails userDetails = principalDetailsService.loadUserByUsername(customUser.username());
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails,userDetails.getPassword(),userDetails.getAuthorities());
        context.setAuthentication(authentication);
        return context;
    }

}
