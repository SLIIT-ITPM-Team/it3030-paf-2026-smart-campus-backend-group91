package com.smart_campus_hub.smart_campus_api.config;

import com.smart_campus_hub.smart_campus_api.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class GoogleOAuthSuccessHandler implements AuthenticationSuccessHandler {

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    private final AuthService authService;

    public GoogleOAuthSuccessHandler(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;
        OAuth2User oauth2User = oauth2Token.getPrincipal();

        String email = oauth2User.getAttribute("email");
        String name  = oauth2User.getAttribute("name");

        if (email == null || email.isBlank()) {
            response.sendRedirect(frontendUrl + "/login?error=google_no_email");
            return;
        }

        String appToken = authService.loginOrRegisterOAuthUser(email, name);
        response.sendRedirect(frontendUrl + "/auth/callback?token=" + appToken);
    }
}
