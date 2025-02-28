package com.example.Default_Project.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

@Slf4j
public class CustomAuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private static final String SESSION_KEY = "OAUTH2_AUTHORIZATION_REQUEST";

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        Object sessionData = request.getSession().getAttribute(SESSION_KEY);
        log.info("Session Loaded: {}", sessionData);
        return (OAuth2AuthorizationRequest) sessionData;
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request, HttpServletResponse response) {
        if (authorizationRequest == null) {
            log.info("Authorization Request Removed");
            request.getSession().removeAttribute(SESSION_KEY);
        } else {
            log.info("Authorization Request Saved: {}", authorizationRequest);
            request.getSession().setAttribute(SESSION_KEY, authorizationRequest);
        }
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
        OAuth2AuthorizationRequest authRequest = loadAuthorizationRequest(request);
        request.getSession().removeAttribute(SESSION_KEY);
        log.info("Authorization Request Removed From Session: {}", authRequest);
        return authRequest;
    }
}