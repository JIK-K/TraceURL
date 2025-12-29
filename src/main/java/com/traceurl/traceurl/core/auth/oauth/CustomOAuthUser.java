package com.traceurl.traceurl.core.auth.oauth;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

@Getter
public class CustomOAuthUser implements OAuth2User {
    private final UUID userId;
    private final String email;
    private final OAuth2User delegate;

    public CustomOAuthUser(UUID userId, String email, OAuth2User delegate){
        this.userId = userId;
        this.email = email;
        this.delegate = delegate;
    }

    @Override
    public Map<String, Object> getAttributes(){
        return delegate.getAttributes();
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities(){
        return delegate.getAuthorities();
    }

    @Override
    public String getName(){
        return userId.toString();
    }
}
