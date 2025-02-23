package com.example.Default_Project.controller;

import com.example.Default_Project.jwt.JWTUtil;
import com.example.Default_Project.repository.AuthRepository;
import com.example.Default_Project.utils.JwtConstants;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final JWTUtil jwtUtil;
    private final AuthRepository authRepository;

    @PostMapping("/token-refresh")
    public ResponseEntity<?> tokenRefresh(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("토큰 리프레쉬 요청");
        String header_accessToken = request.getHeader(JwtConstants.AUTHORIZATION_HEADER_KEY);
        String cookie_accessToken = jwtUtil.getAccessTokenByCookie(request);

        // 리프레쉬 토큰으로 엑세스 토큰을 재발급 받으려면 엑세스토큰이 만료되었더라도 있어야한다.
        // header_accessToke == 단순 refresh 요청
        // cookie_accessToken == SNS Oauth2 로그인후 요청
        if (header_accessToken == null && cookie_accessToken == null) {
            //response status code
            // 만약 없다면 에러처리 [이건 잘못된 접근임]
            return new ResponseEntity<>("access token is null", HttpStatus.BAD_REQUEST);
        }

        if (cookie_accessToken != null) {
            jwtUtil.deleteCookie(response, JwtConstants.ACCESS);
        }

        //get refresh token
        String refresh = jwtUtil.getRefreshToken(request);

        if (refresh == null) {
            // 만약 없다면 에러처리 [이건 잘못된 접근임]
            //response status code
            return new ResponseEntity<>("refresh token null", HttpStatus.BAD_REQUEST);
        }

        //expired check
        try {
            // 만료되었을시 세션이 만료되었음으로 다시로그인하라고 말해줘야함
            jwtUtil.isExpired(refresh);
        } catch (ExpiredJwtException e) {

            //response status code
            return new ResponseEntity<>("refresh token expired", HttpStatus.BAD_REQUEST);
        }

        //DB에 저장되어 있는지 확인
        Boolean isExist = authRepository.existsByRefreshToken(refresh);
        if (!isExist) {
            //response body
            return new ResponseEntity<>("invalid refresh token", HttpStatus.BAD_REQUEST);
        }

        // 토큰이 refresh인지 확인 (발급시 페이로드에 명시)
        String category = jwtUtil.getCategory(refresh);

        if (!category.equals(JwtConstants.REFRESH)) {
            // 리프레쉬가 아닌 다른 토큰을 사용했을시 잘못된 접근
            //response status code
            return new ResponseEntity<>("invalid refresh token", HttpStatus.BAD_REQUEST);
        }

        String username = jwtUtil.getUsername(refresh);
        String role = jwtUtil.getRole(refresh);

        String newAccess = jwtUtil.createJwt(JwtConstants.ACCESS, username, role, JwtConstants.ACCESS_EXPIRED_MS);
        String newRefresh = jwtUtil.createJwt(JwtConstants.REFRESH, username, role, JwtConstants.REFRESH_EXPIRED_MS);

        //기존 Refresh 토큰 DB에서 삭제
        authRepository.deleteByRefreshToken(refresh);

        //새로운 refresh 생성
        jwtUtil.addRefreshEntity(username, newRefresh, JwtConstants.REFRESH_EXPIRED_MS);

        // 쿠키에 새로발급한 리프레쉬 토큰 저장
        jwtUtil.addCookieRefreshToken(newRefresh, response, JwtConstants.REFRESH_EXPIRED_MS);

        // 헤더에 새로발급한 엑세스 토큰 저장
        jwtUtil.addHeaderAccessToken(newAccess, response);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
