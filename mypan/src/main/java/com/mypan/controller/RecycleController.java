package com.mypan.controller;

import com.mypan.annotation.GlobalInterceptor;
import com.mypan.annotation.VerifyParam;
import com.mypan.entity.dto.SessionWebUserDto;
import com.mypan.entity.query.FileInfoQuery;
import com.mypan.entity.vo.FileInfoVO;
import com.mypan.entity.vo.PaginationResultVO;
import com.mypan.entity.vo.ResponseVO;
import com.mypan.enums.FileDelFlag;
import com.mypan.service.FileInfoService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

@RestController("recycleController")
@RequestMapping("/recycle")
public class RecycleController extends ABaseController{
    @Resource
    private FileInfoService fileInfoService;

    @RequestMapping("/loadRecycleList")
    @GlobalInterceptor
    public ResponseVO loadRecycleList(HttpSession session,Integer pageNo,Integer pageSize){
        FileInfoQuery fileInfoQuery=new FileInfoQuery();
        fileInfoQuery.setPageNo(pageNo);
        fileInfoQuery.setPageSize(pageSize);
        fileInfoQuery.setUserId(getUserInfoFromSession(session).getUserId());
        fileInfoQuery.setOrderBy("recovery_time desc");
        fileInfoQuery.setDelFlag(FileDelFlag.RECYCLE.getFlag());
        PaginationResultVO resultVO=fileInfoService.findListByPage(fileInfoQuery);
        return getSuccessResponseVO(convert2PaginationResultVO(resultVO, FileInfoVO.class));
    }

    @RequestMapping("/recoverFile")
    @GlobalInterceptor
    public ResponseVO recoverFile(HttpSession session, @VerifyParam(required = true) String fileIds){
        SessionWebUserDto webUserDto=getUserInfoFromSession(session);
        fileInfoService.recoverFileBatch(webUserDto.getUserId(), fileIds);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/delFile")
    @GlobalInterceptor
    public ResponseVO delFile(HttpSession session, @VerifyParam(required = true) String fileIds){
        SessionWebUserDto webUserDto=getUserInfoFromSession(session);
        fileInfoService.delFileBatch(webUserDto.getUserId(), fileIds,false);
        return getSuccessResponseVO(null);
    }
}
