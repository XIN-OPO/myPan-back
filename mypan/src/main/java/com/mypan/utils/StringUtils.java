package com.mypan.utils;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.Random;

public class StringUtils {
    //生成随机数
    public static final String getRandomNumber(Integer count){
        return RandomStringUtils.random(count,false,true);
    }
}
