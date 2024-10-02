package com.mypan.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.Random;

public class StringUtils {
    //生成随机数
    public static final String getRandomNumber(Integer count){
        return RandomStringUtils.random(count,false,true);
    }

    public static boolean isEmpty(String str){
        if(null==str || "".equals(str) || "null".equals(str) || "\u0000".equals(str)){
            return true;
        }else if("".equals(str.trim())){
            return true;
        }
        return false;
    }

    public static String encodeByMD5(String originStr){
        return isEmpty(originStr)?null: DigestUtils.md5Hex(originStr);
    }
}
