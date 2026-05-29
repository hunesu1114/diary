package com.danji.diary.security;

import com.danji.diary.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final String frontendUrl;

    public OAuth2SuccessHandler(JwtTokenProvider tokenProvider,
                                UserRepository userRepository,
                                @Value("${app.frontend-url}") String frontendUrl) {
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
        this.frontendUrl = frontendUrl;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User principal = (OAuth2User) authentication.getPrincipal();
        String kakaoId = String.valueOf(principal.getAttributes().get("id"));

        Long userId = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new IllegalStateException("User not found after OAuth login"))
                .getId();

        String token = tokenProvider.createToken(userId);

        // 프론트 콜백 페이지로 토큰 전달
        String target = UriComponentsBuilder.fromUriString(frontendUrl)
                .path("/oauth/callback")
                .queryParam("token", token)
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, target);
    }
}
