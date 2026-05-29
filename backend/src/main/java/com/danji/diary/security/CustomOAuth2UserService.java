package com.danji.diary.security;

import com.danji.diary.domain.User;
import com.danji.diary.repository.UserRepository;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @SuppressWarnings("unchecked")
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String kakaoId = String.valueOf(attributes.get("id"));

        Map<String, Object> properties = (Map<String, Object>) attributes.getOrDefault("properties", Map.of());
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.getOrDefault("kakao_account", Map.of());

        String nickname = properties.get("nickname") != null ? String.valueOf(properties.get("nickname")) : "사용자";
        String email = kakaoAccount.get("email") != null ? String.valueOf(kakaoAccount.get("email")) : null;

        User user = userRepository.findByKakaoId(kakaoId)
                .map(u -> {
                    u.setNickname(nickname);
                    if (email != null) u.setEmail(email);
                    return u;
                })
                .orElseGet(() -> User.builder()
                        .kakaoId(kakaoId)
                        .nickname(nickname)
                        .email(email)
                        .build());
        userRepository.save(user);

        return new DefaultOAuth2User(
                AuthorityUtils.createAuthorityList("ROLE_USER"),
                attributes,
                "id");
    }
}
