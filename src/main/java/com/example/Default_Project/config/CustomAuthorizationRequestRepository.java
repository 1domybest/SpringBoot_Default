package com.example.Default_Project.config;

import com.example.Default_Project.repository.AuthRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import java.io.*;
import java.util.Base64;

@Slf4j
@RequiredArgsConstructor
public class CustomAuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private static final String SESSION_KEY = "OAUTH2_AUTHORIZATION_REQUEST";
    public static final String OAUTH_2_AUTHORIZATION_REQUEST = "oauth2_authorization_request";


    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(OAUTH_2_AUTHORIZATION_REQUEST)) {

                OAuth2AuthorizationRequest oauth2Request = deserialize(cookie.getValue());

                // 사용후 삭제
                cookie.setSecure(true); // HTTPS 요청만
                cookie.setHttpOnly(true); // JavaScript 접근 차단
                cookie.setPath("/");
                cookie.setMaxAge(0);
                cookie.setAttribute("SameSite", "None");

                response.addCookie(cookie);
                return oauth2Request;
            }
        }
        throw new RuntimeException("cookie not found");
    }

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return null;
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request, HttpServletResponse response) {
        if (authorizationRequest == null) {
            for (Cookie cookie : request.getCookies()) {
                cookie.setMaxAge(0);
            }
        }

        Cookie cookie = new Cookie(OAUTH_2_AUTHORIZATION_REQUEST, serialize(authorizationRequest));
        cookie.setHttpOnly(true);
        cookie.setMaxAge(10);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
        OAuth2AuthorizationRequest authRequest = loadAuthorizationRequest(request, response);
        request.getSession().removeAttribute(SESSION_KEY);
        log.info("Authorization Request Removed From Session: {}", authRequest);
        return authRequest;
    }

    public static String serialize(OAuth2AuthorizationRequest request) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {

            oos.writeObject(request);
            oos.flush();
            return Base64.getEncoder().encodeToString(bos.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize OAuth2AuthorizationRequest", e);
        }
    }

    public static OAuth2AuthorizationRequest deserialize(String encoded) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(Base64.getDecoder().decode(encoded));
             ObjectInputStream ois = new ObjectInputStream(bis)) {

            return (OAuth2AuthorizationRequest) ois.readObject();

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to deserialize OAuth2AuthorizationRequest", e);
        }
    }
}