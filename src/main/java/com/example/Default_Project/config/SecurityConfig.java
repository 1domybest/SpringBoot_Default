package com.example.Default_Project.config;

import com.example.Default_Project.filter.CustomLoginFilter;
import com.example.Default_Project.filter.CustomLogoutFilter;
import com.example.Default_Project.filter.JWTFilter;
import com.example.Default_Project.jwt.JWTUtil;
import com.example.Default_Project.ouath2.CustomSuccessHandler;
import com.example.Default_Project.repository.AuthRepository;
import com.example.Default_Project.service.snsServices.CustomOAuth2UserService;
import com.example.Default_Project.utils.CommonConstants;
import com.example.Default_Project.utils.JwtConstants;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

/**
 * 스프링 시큐리티
 */
@Configuration // 설정파일이다
@EnableWebSecurity // 시큐리티 파일이다
@RequiredArgsConstructor
public class SecurityConfig {

    /**
     * OAuth2 서비스와 핸들러
     */
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomSuccessHandler customSuccessHandler;

    /**
     * 시큐리티 검증설정 객체
     */
    private final AuthenticationConfiguration authenticationConfiguration;

    /**
     * JWT 관련 유틸함수 모음
     */
    private final JWTUtil jwtUtil;

    /**
     * 토큰저장 전용 Entity
     */
    private final AuthRepository authRepository;


    /**
     * @param authenticationConfiguration 보안 검증설정 객체
     * @return AuthenticationManager 검증객체 매니저
     * @throws Exception 예외
     * @see SecurityFilterChain 의 아래 filterChain 가 실행되고 http.build 된후 호출되는 함수
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        System.out.println("JWT log: " + "SecurityConfig authenticationManager");
        return authenticationConfiguration.getAuthenticationManager();
    }


    /**
     * 비밀번호 암호화
     *
     * @return BCryptPasswordEncoder
     */
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        System.out.println("JWT log: " + "SecurityConfig bCryptPasswordEncoder");
        return new BCryptPasswordEncoder();
    }

    /**
     * 시큐리티 필터체인 설정
     * 이곳에서 보안설정을 한다
     * 앱이 실행되고 단 1번 호출됨
     *
     * @return SecurityFilterChain
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        System.out.println("JWT log: " + "SecurityConfig filterChain");

        // CORS 설정
        http.cors((cors) -> cors
                .configurationSource(request -> {
                    CorsConfiguration configuration = new CorsConfiguration();
                    configuration.setAllowedOriginPatterns(Collections.singletonList("*")); // 모든 origin 허용 (테스트용)
                    // configuration.setAllowedOrigins(Collections.singletonList(CommonConstants.WEB_CLIENT_URL)); // React 클라이언트 URL
                    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    configuration.setAllowedMethods(Collections.singletonList("*")); // 모든 HTTP 메서드 허용
                    configuration.setAllowCredentials(true);
                    configuration.setAllowedHeaders(Collections.singletonList("*")); // 모든 요청 헤더 허용
                    configuration.setExposedHeaders(Collections.singletonList(JwtConstants.AUTHORIZATION_HEADER_KEY)); // Authorization 헤더 노출
                    configuration.setMaxAge(3600L); // 1시간 캐시
                    return configuration;
                })
        );



        //csrf disable
        http
                .csrf((auth) -> auth.disable());

        //From 로그인 방식 disable
        http
                .formLogin((auth) -> auth.disable());

        //HTTP Basic 인증 방식 disable
        http
                .httpBasic((auth) -> auth.disable());


        http.authorizeHttpRequests((auth) -> auth
                .requestMatchers("/api/hc", "/api/env").permitAll() // 무중단 배포용
                .requestMatchers("/api/login", "/api/join").permitAll() // 허용
                .requestMatchers("/api/admin").hasRole("ADMIN")
                .requestMatchers("/api/token-refresh").permitAll()
                // 권한필요 단 토큰이 없는데 여기까지 올일이 없음
                // 단 혹시나 토큰이 없거나 role이 다르다면 바로 다음 필터로 넘어감
                .anyRequest().authenticated() // 나머지는 다 가능 else
        );


        //oauth2
        // 소셜로그인시 아래 필터에 걸림
        http
                .oauth2Login((oauth2) -> oauth2
                        .userInfoEndpoint((userInfoEndpointConfig) -> userInfoEndpointConfig
                                .userService(customOAuth2UserService))
                        .successHandler(customSuccessHandler)
                );


        /*
         * before At after 을 사용하는 이유는 이걸 지정하지않고 At을 사용한다면
         * 동작의 순서가 보장되지 않기때문이다.
         */
        // LoginFilter 가 실행되기 전에 JWTFilter를 실행하겠다
        http.addFilterBefore(new JWTFilter(jwtUtil), CustomLoginFilter.class);

        // LoginFilter 를 즉시 실행하겠다
        http.addFilterAt(new CustomLoginFilter(authenticationManager(authenticationConfiguration), jwtUtil, authRepository),
                UsernamePasswordAuthenticationFilter.class);


        // CustomLogoutFilter 는 LogoutFilter 을 상속받았기때문에 기본적으로 LogoutFilter 가 먼저 실행되고 그안에서
        // 따로 이베트 콜백을 받아서 커스텀한 비지니스 로직이 진행된다.
        http.addFilterBefore(new CustomLogoutFilter(jwtUtil, authRepository), LogoutFilter.class);


        // JWT 방식에서는 상태를 저장하지않기때문에 상태정책에서 빼겠다
        http.sessionManagement((session) -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));


        return http.build();
    }
}