package com.mypan.component;

import com.mypan.entity.constants.Constants;
import com.mypan.entity.dto.DownloadFileDto;
import com.mypan.entity.dto.SysSettingsDto;
import com.mypan.entity.dto.UserSpaceDto;
import com.mypan.entity.po.FileInfo;
import com.mypan.entity.po.UserInfo;
import com.mypan.entity.query.FileInfoQuery;
import com.mypan.entity.query.UserInfoQuery;
import com.mypan.mappers.FileInfoMapper;
import com.mypan.mappers.UserInfoMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component("RedisComponent")
public class RedisComponent {
    @Resource
    private RedisUtils redisUtils;

    @Resource
    private FileInfoMapper<FileInfo, FileInfoQuery> fileInfoMapper;

    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;


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

    public UserSpaceDto resetUserSpaceUse(String userId){
        UserSpaceDto userSpaceDto=new UserSpaceDto();
        Long useSpace=this.fileInfoMapper.selectUseSpace(userId);//查询文件表得使用得容量
        userSpaceDto.setUseSpace(useSpace);
        UserInfo userInfo=this.userInfoMapper.selectByUserId(userId);//查询用户表得总容量
        userSpaceDto.setTotalSpace(userInfo.getTotalSpace());
        redisUtils.setex(Constants.redis_key_user_space_use+userId,userSpaceDto,Constants.redis_key_expires_day);
        return userSpaceDto;
    }

    public UserSpaceDto getUserSpaceUse(String userId){
        UserSpaceDto spaceDto=(UserSpaceDto) redisUtils.get(Constants.redis_key_user_space_use+userId);
        if(spaceDto==null){
            spaceDto=new UserSpaceDto();
            Long useSpace=fileInfoMapper.selectUseSpace(userId);
            spaceDto.setUseSpace(useSpace);
            spaceDto.setTotalSpace(getSysSettingDto().getUserInitUseSpace() * Constants.MB);
            saveUserSpaceUse(userId,spaceDto);
        }
        return spaceDto;
    }

    //获取临时文件大小
    public Long getFileTempSize(String userId ,String fileId){
        Long currentSize=getFileSizeFromRedis(Constants.redis_key_user_file_temp_size+userId+fileId);
        return currentSize;
    }
    private Long getFileSizeFromRedis(String key){
        Object sizeObj=redisUtils.get(key);
        if(sizeObj==null){
            return 0L;
        }
        if(sizeObj instanceof Integer){
            return ((Integer)sizeObj).longValue();
        }else if(sizeObj instanceof Long ){
            return (Long)sizeObj;
        }
        return 0L;
    }
    //保存临时文件大小
    public void saveTempFileSize(String userId,String fileId,Long fileSize){
        Long currentSize=getFileTempSize(userId,fileId);
        redisUtils.setex(Constants.redis_key_user_file_temp_size+userId+fileId,currentSize+fileSize,Constants.redis_key_expires_one_hour);
    }

    public void saveDownloadCode(String code, DownloadFileDto downloadFileDto){
        redisUtils.setex(Constants.redis_key_download+code,downloadFileDto,Constants.redis_key_expires_5min);
    }

    public DownloadFileDto getDownloadCode(String code) {
        return (DownloadFileDto) redisUtils.get(Constants.redis_key_download+code);
    }
    public void savaSysSettingDto(SysSettingsDto sysSettingsDto){
        redisUtils.set(Constants.redis_key_sys_setting,sysSettingsDto);
    }


}
