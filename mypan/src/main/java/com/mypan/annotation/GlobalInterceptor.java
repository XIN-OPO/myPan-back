package com.mypan.annotation;

import org.springframework.web.bind.annotation.Mapping;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Mapping
@Inherited
public @interface GlobalInterceptor {
    //校验参数
    boolean checkParams() default false;

    //校验登录
    boolean checkLogin() default true;

    //校验超级管理员
    boolean checkAdmin() default false;
}
