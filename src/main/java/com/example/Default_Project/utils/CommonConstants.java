package com.example.Default_Project.utils;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 공통 전역변수
 */
@Component
public class CommonConstants {

    @Value("${server.clientAddress}")
    private String clientAddress;

    @Value("${server.clientPort}")
    private String clientPort;

    /**
     *  HTTP Method
     */
    public static final String POST = "POST";
    public static final String GET = "GET";
    public static final String PUT = "GET";
    public static final String DELETE = "DELETE";
    public static final String UPDATE = "UPDATE";

    /**
     * Client URL
     */
    public static String CLIENT_ADDRESS;
    public static String CLIENT_PORT;
    public static String WEB_CLIENT_URL;
    public static String WEB_OAUTH2_REDIRECT_URL;

    @PostConstruct
    public void init() {
        CLIENT_ADDRESS = clientAddress;
        CLIENT_PORT = clientPort;
        WEB_CLIENT_URL = "http://" + CLIENT_ADDRESS + ":" + CLIENT_PORT;
        WEB_OAUTH2_REDIRECT_URL = WEB_CLIENT_URL + "/oauth2/succeed";
    }
}
