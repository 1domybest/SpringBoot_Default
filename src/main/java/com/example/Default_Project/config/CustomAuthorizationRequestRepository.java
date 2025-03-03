package com.example.Default_Project.config;

import com.example.Default_Project.entity.AuthEntity;
import com.example.Default_Project.repository.AuthRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import java.io.*;
import java.util.Base64;
import java.util.Collections;

@Slf4j
@RequiredArgsConstructor
public class CustomAuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private static final String SESSION_KEY = "OAUTH2_AUTHORIZATION_REQUEST";

    /**
     * 토큰저장 전용 Entity
     */
    private final AuthRepository authRepository;

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        String state = request.getParameter("state");

        String authorizationRequestString = authRepository.findByState(state).getAuthorizationRequest();
        log.info("요청받은 state: {}", authorizationRequestString);
        OAuth2AuthorizationRequest authorizationRequest = deserialize(authorizationRequestString);
        System.out.println("클라이언트 아이디" + authorizationRequest.getClientId());

        Object sessionData = request.getSession().getAttribute(SESSION_KEY);
        log.info("Session Loaded: {}", sessionData);  // 세션에 저장된 값 로드
        return authorizationRequest;
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request, HttpServletResponse response) {
        boolean isNewSession = request.getSession(false) == null;
        log.info("Is New Session: {}", isNewSession);  // 새로운 세션인지 확인
        log.info("Session ID: {}", request.getSession().getId());
        log.info("Session Attributes: {}", Collections.list(request.getSession().getAttributeNames()));

        String serialized = serialize(authorizationRequest);
        System.out.println("serialized: " + serialized);
        AuthEntity authEntity = new AuthEntity();
        authEntity.setAuthorizationRequest(serialized);
        authEntity.setState(authorizationRequest.getState());
        authRepository.save(authEntity);


        if (authorizationRequest == null) {
            log.info("Authorization Request Removed");
            request.getSession().removeAttribute(SESSION_KEY);
        } else {
            log.info("Authorization Request Saved: {}", authorizationRequest);
            log.info("Authorization Request State: {}", authorizationRequest.getState());
            request.getSession().setAttribute(SESSION_KEY, authorizationRequest);

            Object sessionData = request.getSession().getAttribute(SESSION_KEY);
            log.info("방금 저장된 세션: {}", sessionData);  // 세션에 저장된 값 로드

        }
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
        OAuth2AuthorizationRequest authRequest = loadAuthorizationRequest(request);
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