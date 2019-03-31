package com.github.wuchao.activiti.security;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Data
public class CustomUser extends User {

    private static final long serialVersionUID = 1L;

    private long userId;
    private String nickname;

    public CustomUser(long userId, String nickname, String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.userId = userId;
        this.nickname = nickname;
    }

}
