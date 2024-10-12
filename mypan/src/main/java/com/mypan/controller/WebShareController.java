package com.mypan.controller;

import ch.qos.logback.core.joran.util.beans.BeanUtil;
import com.mypan.annotation.GlobalInterceptor;
import com.mypan.annotation.VerifyParam;
import com.mypan.entity.constants.Constants;
import com.mypan.entity.dto.SessionShareDto;
import com.mypan.entity.dto.SessionWebUserDto;
import com.mypan.entity.po.FileInfo;
import com.mypan.entity.po.FileShare;
import com.mypan.entity.po.UserInfo;
import com.mypan.entity.query.FileInfoQuery;
import com.mypan.entity.vo.FileInfoVO;
import com.mypan.entity.vo.PaginationResultVO;
import com.mypan.entity.vo.ResponseVO;
import com.mypan.entity.vo.ShareInfoVo;
import com.mypan.enums.FileDelFlag;
import com.mypan.enums.ResponseCodeEnum;
import com.mypan.exception.BusinessException;
import com.mypan.service.FileInfoService;
import com.mypan.service.FileShareService;
import com.mypan.service.UserInfoService;
import com.mypan.utils.CopyTools;
import com.mypan.utils.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Date;

@RestController("webShareController")
@RequestMapping("/showShare")
public class WebShareController extends CommonFileController{

    @Resource
    private FileShareService fileShareService;

    @Resource
    private FileInfoService fileInfoService;

    @Resource
    private UserInfoService userInfoService;

    @RequestMapping("/getShareLoginInfo")
    @GlobalInterceptor(checkParams = true,checkLogin = false)
    public ResponseVO getShareLoginInfo(HttpSession session,@VerifyParam(required = true) String shareId) throws BusinessException {
        SessionShareDto sessionShareDto=getSessionShareFromSession(session,shareId);
        if(sessionShareDto==null){
            return getSuccessResponseVO(null);
        }
        //判断是否是当前用户上传的文件
        ShareInfoVo shareInfoVo=getShareInfoCommon(shareId);
        SessionWebUserDto sessionWebUserDto=getUserInfoFromSession(session);
        if(sessionWebUserDto!=null && sessionWebUserDto.getUserId().equals(sessionShareDto.getShareUserId())){
            shareInfoVo.setCurrentUser(true);
        }else {
            shareInfoVo.setCurrentUser(false);
        }

        return getSuccessResponseVO(shareInfoVo);
    }

    @RequestMapping("/getShareInfo")
    @GlobalInterceptor(checkParams = true,checkLogin = false)
    public ResponseVO getShareInfo(@VerifyParam(required = true) String shareId) throws BusinessException {
        return getSuccessResponseVO(getShareInfoCommon(shareId));
    }

    private ShareInfoVo getShareInfoCommon(String shareId) throws BusinessException {
        FileShare share=fileShareService.getFileShareByShareId(shareId);
        if(null==share || (share.getExpireTime()!=null && new Date().after(share.getExpireTime()))){
            throw new BusinessException(ResponseCodeEnum.CODE_902.getMsg());
        }
        ShareInfoVo shareInfoVo= CopyTools.copy(share,ShareInfoVo.class);
        FileInfo fileInfo=fileInfoService.getFileInfoByFileIdAndUserId(share.getFileId(),share.getUserId());
        if(fileInfo==null || !FileDelFlag.USING.getFlag().equals(fileInfo.getDelFlag())){
            throw new BusinessException(ResponseCodeEnum.CODE_902.getMsg());
        }
        shareInfoVo.setFileName(fileInfo.getFileName());
        UserInfo userInfo  = userInfoService.getUserInfoByUserId(share.getUserId());
        shareInfoVo.setNickName(userInfo.getNickName());
        shareInfoVo.setAvatar(userInfo.getQqAvatar());
        shareInfoVo.setUserId(userInfo.getUserId());
        return shareInfoVo;
    }

    @RequestMapping("/checkShareCode")
    @GlobalInterceptor(checkParams = true,checkLogin = false)
    public ResponseVO checkShareCode(HttpSession session,
                                     @VerifyParam(required = true) String shareId,
                                     @VerifyParam(required = true) String code) throws BusinessException {
        SessionShareDto sessionShareDto=fileShareService.checkShareCode(shareId,code);
        session.setAttribute(Constants.session_share_key+shareId,sessionShareDto);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/loadFileList")
    @GlobalInterceptor(checkParams = true,checkLogin = false)
    private ResponseVO loadFileList(HttpSession session,
                                    @VerifyParam(required = true) String shareId,
                                     String filePid) throws BusinessException {

        SessionShareDto sessionShareDto=checkShare(session,shareId);
        System.out.println(sessionShareDto);
        FileInfoQuery query= new FileInfoQuery();

        if(!StringUtils.isEmpty(filePid) && !Constants.zero_str.equals(filePid)){
            this.fileInfoService.checkRootFilePid(sessionShareDto.getFileId(),sessionShareDto.getShareUserId(),filePid);
            query.setFilePid(filePid);
        }else {
            query.setFileId(sessionShareDto.getFileId());
        }
        query.setUserId(sessionShareDto.getShareUserId());
        query.setOrderBy("last_update_time desc");
        query.setDelFlag(FileDelFlag.USING.getFlag());
        PaginationResultVO resultVO=this.fileInfoService.findListByPage(query);
        return getSuccessResponseVO(convert2PaginationResultVO(resultVO, FileInfoVO.class));
    }

    private SessionShareDto checkShare(HttpSession session,String shareId) throws BusinessException {
        SessionShareDto sessionShareDto=getSessionShareFromSession(session,shareId);
        if(null==sessionShareDto){
            throw new BusinessException(ResponseCodeEnum.CODE_903);
        }
        if(sessionShareDto.getExpireTime()!=null && new Date().after(sessionShareDto.getExpireTime())){
            throw new BusinessException(ResponseCodeEnum.CODE_902);
        }
        return sessionShareDto;
    }

    @RequestMapping("/getFolderInfo")
    @GlobalInterceptor(checkParams = true,checkLogin = false)
    public ResponseVO getFolderInfo(HttpSession session,
                                    @VerifyParam(required = true) String shareId,
                                    @VerifyParam(required = true) String path) throws BusinessException {
        SessionShareDto sessionShareDto =checkShare(session,shareId);
        return super.getFolderInfo(path,sessionShareDto.getShareUserId());
    }

    @RequestMapping("/getFile/{shareId}/{fileId}")
    @GlobalInterceptor(checkParams = true,checkAdmin = true)
    public void getFile(HttpSession session,HttpServletResponse response ,@PathVariable("shareId") String shareId,
                        @PathVariable("fileId") String fileId) throws BusinessException {
        SessionShareDto sessionShareDto =checkShare(session,shareId);
        super.getFile(response,fileId,sessionShareDto.getShareUserId());
    }

    @RequestMapping("/ts/getVideoInfo/{shareId}/{fileId}")
    @GlobalInterceptor(checkParams = true,checkLogin = false)
    public void getImage(HttpSession session,HttpServletResponse response ,@PathVariable("shareId") String shareId,
                         @PathVariable("fileId") String fileId) throws BusinessException {
        SessionShareDto sessionShareDto =checkShare(session,shareId);
        super.getFile(response,fileId,sessionShareDto.getShareUserId());
    }

    @RequestMapping("/createDownloadUrl/{shareId}/{fileId}")
    @GlobalInterceptor(checkParams = true,checkLogin = false)
    public ResponseVO createDownloadUrl(HttpSession session,
            @PathVariable("shareId") String shareId,
            @PathVariable("fileId") String fileId
    ) throws BusinessException {
        SessionShareDto sessionShareDto =checkShare(session,shareId);
        return super.createDownloadUrl(fileId,sessionShareDto.getShareUserId());
    }

    @RequestMapping("/download/{code}")
    @GlobalInterceptor(checkParams = true,checkLogin = false)
    public void download(HttpServletResponse response, HttpServletRequest request,
                         @VerifyParam(required = true) @PathVariable("code") String code

    ) throws Exception {
        super.download(request,response,code);
    }

    @RequestMapping("/saveShare")
    @GlobalInterceptor(checkParams = true,checkLogin = false)
    public ResponseVO saveShare(HttpSession session,
                          @VerifyParam(required = true) String shareId,
                          @VerifyParam(required = true) String shareFileIds,
                          @VerifyParam(required = true) String myFolderId) throws BusinessException {
        SessionShareDto sessionShareDto =checkShare(session,shareId);
        SessionWebUserDto sessionWebUserDto=getUserInfoFromSession(session);
        if(sessionShareDto.getShareUserId().equals(sessionWebUserDto.getUserId())){
            throw new BusinessException("自己分享的文件无法保存到自己的网盘中");
        }
        fileInfoService.saveShare(sessionShareDto.getFileId(),shareFileIds,myFolderId,sessionShareDto.getShareUserId(), sessionWebUserDto.getUserId());
        return getSuccessResponseVO(null);
    }
}
