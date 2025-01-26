package com.example.Default_Project.service.snsServices.naver;


import com.example.Default_Project.service.snsServices.OAuth2Response;

import java.util.Map;

public class NaverResponse implements OAuth2Response {

    private final Map<String, Object> attributes;

    public NaverResponse(Map<String, Object> attributes) {
        this.attributes = (Map<String, Object>)attributes.get("response");
    }

    @Override
    public String getProvider() {
        return "naver";
    }

    @Override
    public String getProviderId() {
        return this.attributes.get("id").toString();
    }

    @Override
    public String getEmail() {
        return this.attributes.get("email").toString();
    }

    @Override
    public String getName() {
        return this.attributes.get("name").toString();
    }

}
