package com.example.Default_Project.utils;

/**
 * 공통 전역변수
 */
public class CommonConstants {
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
    public static final String WEB_CLIENT_PATH = "http://localhost:";
    public static final String WEB_CLIENT_PORT = "3000";
    public static final String WEB_CLIENT_URL = WEB_CLIENT_PATH+WEB_CLIENT_PORT;

    public static final String WEB_OAUTH2_REDIRECT_URL = WEB_CLIENT_URL + "/oauth2/succeed";
}
