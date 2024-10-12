package com.mypan.controller;

import com.mypan.annotation.GlobalInterceptor;
import com.mypan.annotation.VerifyParam;
import com.mypan.component.RedisComponent;
import com.mypan.entity.dto.SessionWebUserDto;
import com.mypan.entity.dto.SysSettingsDto;
import com.mypan.entity.query.FileInfoQuery;
import com.mypan.entity.query.UserInfoQuery;
import com.mypan.entity.vo.PaginationResultVO;
import com.mypan.entity.vo.ResponseVO;
import com.mypan.entity.vo.userInfoVo;
import com.mypan.exception.BusinessException;
import com.mypan.service.FileInfoService;
import com.mypan.service.UserInfoService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@RestController("adminController")
@RequestMapping("/admin")
public class AdminController extends CommonFileController{

    @Resource
    private FileInfoService fileInfoService;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private UserInfoService userInfoService;

    @RequestMapping("/getSysSettings")
    @GlobalInterceptor(checkParams = true,checkAdmin = true)
    public ResponseVO getSysSettings(){
        return getSuccessResponseVO(redisComponent.getSysSettingDto());
    }

    @RequestMapping("/saveSysSettings")
    @GlobalInterceptor(checkParams = true,checkAdmin = true)
    public ResponseVO saveSysSettings(@VerifyParam(required = true) String registerEmailTitle,
                                      @VerifyParam(required = true) String registerEmailContent,
                                      @VerifyParam(required = true) Integer userInitUseSpace){
        SysSettingsDto sysSettingsDto=new SysSettingsDto();
        sysSettingsDto.setRegisterEMailTitle(registerEmailTitle);
        sysSettingsDto.setRegisterEmailContent(registerEmailContent);
        sysSettingsDto.setUserInitUseSpace(userInitUseSpace);
        redisComponent.savaSysSettingDto(sysSettingsDto);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/loadUserList")
    @GlobalInterceptor(checkParams = true,checkAdmin = true)
    public ResponseVO loadUserList(UserInfoQuery userInfoQuery){
        userInfoQuery.setOrderBy("join_time desc");
        PaginationResultVO resultVO=userInfoService.findListByPage(userInfoQuery);
        return getSuccessResponseVO(convert2PaginationResultVO(resultVO, userInfoVo.class));
    }

    @RequestMapping("/updateUserStatus")
    @GlobalInterceptor(checkParams = true,checkAdmin = true)
    public ResponseVO updateUserStatus(@VerifyParam(required = true)String userId,
                                       @VerifyParam(required = true)Integer status){
        userInfoService.updateUserStatus(userId,status);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/updateUserSpace")
    @GlobalInterceptor(checkParams = true,checkAdmin = true)
    public ResponseVO updateUserSpace(@VerifyParam(required = true) String userId,
                                       @VerifyParam(required = true) Integer changeSpace){
        userInfoService.changeUserSpace(userId,changeSpace);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/loadFileList")
    @GlobalInterceptor(checkParams = true,checkAdmin = true)
    public ResponseVO loadFileList(@VerifyParam(required = true)FileInfoQuery fileInfoQuery){
        fileInfoQuery.setOrderBy("last_update_time desc");
        fileInfoQuery.setQueryNickName(true);
        PaginationResultVO resultVO=fileInfoService.findListByPage(fileInfoQuery);
        return getSuccessResponseVO(resultVO);
    }

    @RequestMapping("/getFolderInfo")
    @GlobalInterceptor(checkParams = true,checkAdmin = true)
    public ResponseVO getFolderInfo(@VerifyParam(required = true)String path){
        return super.getFolderInfo(path,null);
    }

    @RequestMapping("/getFile/{userId}/{fileId}")
    @GlobalInterceptor(checkParams = true,checkAdmin = true)
    public void getFile(HttpServletResponse response ,@PathVariable("userId") String userId,
                              @PathVariable("fileId") String fileId){

        super.getFile(response,fileId,userId);
    }

    @RequestMapping("/ts/getVideoInfo/{userId}/{fileId}")
    @GlobalInterceptor(checkParams = true,checkAdmin = true)
    public void getImage(HttpServletResponse response ,@PathVariable("userId") String userId,
                        @PathVariable("fileId") String fileId){

        super.getFile(response,fileId,userId);
    }

    @RequestMapping("/createDownloadUrl/{userId}/{fileId}")
    @GlobalInterceptor(checkParams = true,checkAdmin = true)
    public ResponseVO createDownloadUrl(
            @PathVariable("userId") String userId,
            @PathVariable("fileId") String fileId
    ) throws BusinessException {
        return super.createDownloadUrl(fileId,userId);
    }

    @RequestMapping("/download/{code}")
    @GlobalInterceptor(checkParams = true,checkLogin = false)
    public void download(HttpServletResponse response, HttpServletRequest request,
                         @VerifyParam(required = true) @PathVariable("code") String code

    ) throws Exception {
        super.download(request,response,code);
    }

    @RequestMapping("/delFile")
    @GlobalInterceptor(checkParams = true,checkAdmin = true)
    public ResponseVO delFile(HttpSession session,
                              @VerifyParam(required = true) String fileIdsAndUserIds

    ) throws Exception {
        String[] fileIdAndUserIdArray=fileIdsAndUserIds.split(",");
        for(String fileIdAndUserId:fileIdAndUserIdArray){
            String[] itemArray=fileIdAndUserId.split("_");
            fileInfoService.delFileBatch(itemArray[0],itemArray[1],true);
        }
        return getSuccessResponseVO(null);
    }
}
