package com.example.Default_Project.ouath2;

import com.example.Default_Project.jwt.JWTUtil;
import com.example.Default_Project.service.snsServices.CustomOAuth2User;
import com.example.Default_Project.utils.CommonConstants;
import com.example.Default_Project.utils.JwtConstants;
import jakarta.servlet.ServletException;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

@Component
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;

    public CustomSuccessHandler(JWTUtil jwtUtil) {

        this.jwtUtil = jwtUtil;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException, IOException {

        //OAuth2User
        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();

        String username = customUserDetails.getUsername();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        String access = jwtUtil.createJwt(JwtConstants.ACCESS, username, role, JwtConstants.ACCESS_EXPIRED_MS);
        String refresh = jwtUtil.createJwt(JwtConstants.REFRESH, username, role, JwtConstants.REFRESH_EXPIRED_MS);

        //새로운 refresh 생성
        jwtUtil.addRefreshEntity(username, refresh, JwtConstants.REFRESH_EXPIRED_MS);

        // 쿠키에 새로발급한 리프레쉬 토큰 저장
        jwtUtil.addCookieRefreshToken(refresh, response, JwtConstants.REFRESH_EXPIRED_MS);

        // 쿠키에 새로발급한 엑세스 토큰 저장
        // 어차피 다시 재발급 받을예정임으로 만료기간은 1초로줌
        jwtUtil.addCookieAccessToken(access, response, JwtConstants.OAUTH2_ACCESS_EXPIRED_MS);

        System.out.println("SNS 로그인 성공 " + access + jwtUtil.getAccessTokenByCookie(request));
        response.setStatus(HttpStatus.OK.value());

        response.sendRedirect(CommonConstants.WEB_OAUTH2_REDIRECT_URL);
    }
}