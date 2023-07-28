package com.skklub.admin;

import com.skklub.admin.domain.enums.Role;
import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory.class)
public @interface WithMockCustomUser {

    String username() default "mockUserID";
    String password() default "mockPW";
    String name() default "mockUserName";
    Role role() default Role.ROLE_USER;
    String contact() default "mockContact";
}
