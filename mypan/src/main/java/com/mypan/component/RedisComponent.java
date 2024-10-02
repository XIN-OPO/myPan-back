package com.mypan.component;

import com.mypan.entity.constants.Constants;
import com.mypan.entity.dto.SysSettingsDto;
import com.mypan.entity.dto.UserSpaceDto;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component("RedisComponent")
public class RedisComponent {
    @Resource
    private RedisUtils redisUtils;


    public SysSettingsDto getSysSettingDto(){
        SysSettingsDto sysSettingsDto=(SysSettingsDto)redisUtils.get(Constants.redis_key_sys_setting);
        if(null == sysSettingsDto){
            sysSettingsDto= new SysSettingsDto();
            redisUtils.set(Constants.redis_key_sys_setting,sysSettingsDto);
        }
        return sysSettingsDto;

    }
    public void saveUserSpaceUse(String userId, UserSpaceDto userSpaceDto){
        redisUtils.setex(Constants.redis_key_user_space_use+userId,userSpaceDto,Constants.redis_key_expires_day);
    }
}
