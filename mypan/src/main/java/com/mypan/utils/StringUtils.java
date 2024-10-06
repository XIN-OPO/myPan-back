package com.mypan.utils;

import com.mypan.entity.constants.Constants;
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
    public static Boolean pathIsOk(String filePath){
        if (StringUtils.isEmpty(filePath)){
            return true;
        }
        if(filePath.contains("../")||filePath.contains("..\\")){
            return false;
        }
        return true;
    }

    public static String rename(String fileName){
        String fileNameReal=getFileNameNoSuffix(fileName);
        String suffix=getFileSuffix(fileName);
        return fileNameReal+"_"+getRandomNumber(Constants.length_5)+suffix;
    }
    public static String getFileNameNoSuffix(String fileName){
        Integer index=fileName.lastIndexOf(".");
        if(index==-1){
            return fileName;
        }
        fileName=fileName.substring(0,index);
        return fileName;
    }
    public static String getFileSuffix(String fileName){
        Integer index=fileName.indexOf(".");
        if(index==-1){
            return "";
        }
        String suffix=fileName.substring(index);
        return suffix;
    }
}
