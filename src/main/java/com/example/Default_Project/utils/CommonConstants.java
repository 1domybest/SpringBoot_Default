package com.example.Default_Project.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 공통 전역변수
 */
@Component
public class CommonConstants {

    @Value("${server.clientAddress}")
    private static String clientAddress;

    @Value("${server.clientPort}")
    private static String clientPort;

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
    public static final String WEB_CLIENT_PATH = "http://" + clientAddress;
    public static final String WEB_CLIENT_PORT = clientPort;
    public static final String WEB_CLIENT_URL = WEB_CLIENT_PATH+WEB_CLIENT_PORT;

    public static final String WEB_OAUTH2_REDIRECT_URL = WEB_CLIENT_URL + "/oauth2/succeed";
}
