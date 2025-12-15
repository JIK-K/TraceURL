package com.traceurl.traceurl.domain.auth.service;

import com.traceurl.traceurl.domain.auth.oauth.CustomOAuthUser;
import com.traceurl.traceurl.domain.user.entity.User;
import com.traceurl.traceurl.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuthUserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequset) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(userRequset);

        Map<String, Object> atters = oAuth2User.getAttributes();

        String email = (String) atters.get("email");
        String name = (String) atters.get("name");

        User user = userRepository.findByEmail(email)
                .orElseGet(() ->
                        userRepository.save(
                                User.builder()
                                        .email(email)
                                        .displayName(name)
                                        .build()
                        )
                );

        return new CustomOAuthUser(
                user.getId(),
                user.getEmail(),
                new DefaultOAuth2User(
                        Collections.singleton(
                                new SimpleGrantedAuthority("ROLE_USER")
                        ),
                        atters,
                        "sub"
                )
        );
    }
}
