package com.my.blog.website.constant;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "spring.profiles")
@Component
public class ProfileConst {

    public static final String PROFILE_PRODUCT = "prod";
    public static final String PROFILE_DEVELOP = "dev";

    String active;

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }
}
