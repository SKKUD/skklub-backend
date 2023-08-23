package com.skklub.admin;

import com.skklub.admin.domain.User;
import com.skklub.admin.repository.UserRepository;
import com.skklub.admin.security.auth.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TestPrincipalDetailsService implements UserDetailsService{
    private final UserRepository userRepository;
    private final TestDataRepository testDataRepository;

    //시큐리티 session(Authentication(UserDetails))
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if(user!=null){

            return new PrincipalDetails(user);
        }
        throw new UsernameNotFoundException("User "+username+" Not Found");
    }

//    public UserDetails loadUserByUserIndex(Integer userIndex) throws UsernameNotFoundException {
//        User user = testDataRepository.getUsers().get(userIndex);
//        System.out.println(user);
//        if(user!=null){
//
//            return new PrincipalDetails(user);
//        }
//        throw new UsernameNotFoundException("User "+userIndex+" Not Found");
//    }

}
