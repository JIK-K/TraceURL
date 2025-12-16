package com.traceurl.traceurl.core.auth.service;

import com.traceurl.traceurl.core.auth.oauth.CustomOAuthUser;
import com.traceurl.traceurl.core.user.entity.User;
import com.traceurl.traceurl.core.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuthUserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest)
            throws OAuth2AuthenticationException {

        OAuth2User oAuth2User =
                new DefaultOAuth2UserService().loadUser(userRequest);

        String provider =
                userRequest.getClientRegistration().getRegistrationId();

        Map<String, Object> attributes = oAuth2User.getAttributes();

        log.info("OAuth provider: {}", provider);
        log.info("OAuth attributes: {}", attributes);

        String email;
        String name;
        String type;

        if ("google".equals(provider)) {
            email = (String) attributes.get("email");
            name = (String) attributes.get("name");
            type = "GOOGLE";

        } else if ("github".equals(provider)) {
            email = (String) attributes.get("email"); // null 가능
            name = (String) attributes.get("login");
            type = "GITHUB";

            // email이 null이면 임시 처리 (필수 정책에 따라 결정)
            if (email == null) {
                email = name + "@github.com";
            }
        } else {
            throw new OAuth2AuthenticationException("Unsupported provider");
        }

        final String finalEmail = email;
        final String finalName = name;
        final String finalType = type;

        User user = userRepository.findByEmail(finalEmail)
                .orElseGet(() ->
                        userRepository.save(
                                User.builder()
                                        .email(finalEmail)
                                        .displayName(finalName)
                                        .type(finalType)
                                        .build()
                        )
                );
        String nameAttributeKey = userRequest.getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName();

        log.info("Name attribute key: {}", nameAttributeKey);
        log.info("Final email: {}, name: {}, type: {}", finalEmail, finalName, finalType);
        return new CustomOAuthUser(
                user.getId(),
                user.getEmail(),
                new DefaultOAuth2User(
                        Collections.singleton(
                                new SimpleGrantedAuthority("ROLE_USER")
                        ),
                        attributes,
                        nameAttributeKey
                )
        );
    }
}
