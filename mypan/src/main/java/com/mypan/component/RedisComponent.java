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

    public UserSpaceDto getUserSpaceUse(String userId){
        UserSpaceDto spaceDto=(UserSpaceDto) redisUtils.get(Constants.redis_key_user_space_use+userId);
        if(spaceDto==null){
            spaceDto=new UserSpaceDto();
            spaceDto.setUseSpace(0L);
            //TODO 查询当前用户已经上床文件大小的总和
            spaceDto.setTotalSpace(getSysSettingDto().getUserInitUseSpace()*Constants.MB);
            saveUserSpaceUse(userId,spaceDto);
        }
        return spaceDto;
    }
}
