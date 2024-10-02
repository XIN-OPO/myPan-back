package com.mypan.entity.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("appConfig")
public class AppConfig {
    @Value("2081013660@qq.com")
    private String sendUserName;

    @Value("${admin.emails}")
    private String adminEmails;

    public String getSendUserName() {
        return sendUserName;
    }

    public String getAdminEmails(){
        return  adminEmails;
    }
}
