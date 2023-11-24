package com.skklub.admin.security.auth;

import com.skklub.admin.domain.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;

public class PrincipalDetails implements UserDetails {

    //call position
    private User user;

    public PrincipalDetails(User user){
        this.user = user;
    }

    //해당 유저 권한 리턴
    @Override
    public ArrayList<? extends GrantedAuthority> getAuthorities() {
        ArrayList<GrantedAuthority> collect = new ArrayList<>();
        collect.add((GrantedAuthority) () -> String.valueOf(user.getRole()));
        return collect;
    }
    public Long getUserId(){return user.getId();}
    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    //추후 작성 예정(필요한?가?도 잘 모르겠음)
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {return true;}
}
