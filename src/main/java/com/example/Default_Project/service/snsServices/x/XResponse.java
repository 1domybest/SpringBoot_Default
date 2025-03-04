package com.example.Default_Project.service.snsServices.x;


import com.example.Default_Project.service.snsServices.OAuth2Response;

import java.util.Map;

public class XResponse implements OAuth2Response {

    private final Map<String, Object> attributes;

    public XResponse(Map<String, Object> attributes) {
        this.attributes = (Map<String, Object>)attributes.get("data");
    }

    @Override
    public String getProvider() {
        return "x";
    }

    @Override
    public String getProviderId() {
        return this.attributes.get("id").toString();
    }

    @Override
    public String getEmail() {
        return "";
    }

    @Override
    public String getName() {
        return this.attributes.get("name").toString();
    }

}
