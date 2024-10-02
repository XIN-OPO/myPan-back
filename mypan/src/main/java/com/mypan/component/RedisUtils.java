package com.mypan.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

@Component("redisUtils")
public class RedisUtils<V>{
    @Resource
    private RedisTemplate<String,V> redisTemplate;

    private static final Logger logger= LoggerFactory.getLogger(RedisUtils.class);

    //删除缓存
    public void delete(String... key){
        if(key!=null && key.length>0){
            if(key.length==1){
                redisTemplate.delete(key[0]);
            }else {
                redisTemplate.delete((Collection<String>) CollectionUtils.arrayToList(key));
            }
        }
    }

    public V get(String key){
        return key==null?null:redisTemplate.opsForValue().get(key);
    }

    //普通缓存放入
    public boolean set(String key,V value){
        try{
            redisTemplate.opsForValue().set(key,value);
            return true;
        }catch (Exception e){
            logger.error("设置redisKey: {},value: {失败}",key,value);
            return false;
        }
    }

    //普通缓存并设置时间
    public boolean setex(String key, V value, Long time){
        try {
            if(time>0){
                redisTemplate.opsForValue().set(key,value,time, TimeUnit.SECONDS);
            }else {
                set(key,value);
            }
            return true;
        }catch (Exception e){
            logger.error("设置redisKey: {},value: {失败}",key,value);
            return false;
        }
    }
}
