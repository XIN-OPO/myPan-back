package com.mypan.entity.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("appConfig")
public class AppConfig {
    @Value("2081013660@qq.com")
    private String sendUserName;

    @Value("${admin.emails}")
    private String adminEmails;

    @Value("${project.folder:}")
    private String projectFolder;

    public String getSendUserName() {
        return sendUserName;
    }

    public String getAdminEmails(){
        return  adminEmails;
    }
    public String getProjectFolder(){
        return projectFolder;
    }

    //qq登录相关配置
    @Value("${qq.app.id:}")
    private String qqAppId;

    @Value("${qq.app.key}")
    private String qqAppKey;

    @Value("${qq.url.authorization}")
    private String qqUrlAuthorization;

    @Value("${qq.url.access.token}")
    private String qqUrlAccessToken;

    @Value("${qq.url.openid}")
    private String qqUrlOpenid;

    @Value("${qq.url.user.info}")
    private String qqUrlUserInfo;

    @Value("${qq.url.redirect}")
    private String qqUrlRedirect;

    public String getQqAppId() {
        return qqAppId;
    }

    public String getQqAppKey() {
        return qqAppKey;
    }

    public String getQqUrlAuthorization() {
        return qqUrlAuthorization;
    }

    public String getQqUrlAccessToken() {
        return qqUrlAccessToken;
    }

    public String getQqUrlOpenid() {
        return qqUrlOpenid;
    }

    public String getQqUrlUserInfo() {
        return qqUrlUserInfo;
    }

    public String getQqUrlRedirect() {
        return qqUrlRedirect;
    }
}
