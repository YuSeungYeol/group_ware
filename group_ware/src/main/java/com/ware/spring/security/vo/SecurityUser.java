package com.ware.spring.security.vo;

import com.ware.spring.member.domain.Member;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class SecurityUser extends User {

    private final Member member;

    public SecurityUser(Member member, Collection<? extends GrantedAuthority> authorities) {
        super(member.getMemId(), member.getMemPw(), authorities);
        this.member = member;
    }

    public Member getMember() {	
        return member;
    }
    @Override
    public String getPassword() {
        return member.getMemPw(); 
    }
}
