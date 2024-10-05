package com.mypan.controller;

import com.mypan.entity.constants.Constants;
import com.mypan.entity.dto.SessionWebUserDto;
import com.mypan.enums.ResponseCodeEnum;

import com.mypan.entity.vo.ResponseVO;
import com.mypan.exception.BusinessException;
import com.mypan.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ABaseController{
    protected static final String STATUC_SUCCESS="success";
    protected static final String STATUC_ERROR="error";
    private static final Logger logger= LoggerFactory.getLogger(ABaseController.class);
    protected <T> ResponseVO getSuccessResponseVO(T t){
        ResponseVO<T> responseVO =new ResponseVO<>();
        responseVO.setStatus(STATUC_SUCCESS);
        responseVO.setCode(ResponseCodeEnum.CODE_200.getCode());
        responseVO.setInfo(ResponseCodeEnum.CODE_200.getMsg());
        responseVO.setData(t);
        return responseVO;
    }
    protected <T> ResponseVO getBusinessErrorResponse(BusinessException e,T t){
        ResponseVO vo =new ResponseVO();
        vo.setStatus(STATUC_ERROR);
        if(e.getCode()==null){
            vo.setCode(ResponseCodeEnum.CODE_600.getCode());
        }else{
            vo.setCode(e.getCode());
        }
        vo.setInfo(e.getMessage());
        vo.setData(t);
        return vo;
    }
    protected <T> ResponseVO getServeErrorResponseVo(T t){
        ResponseVO vo =new ResponseVO();
        vo.setStatus(STATUC_ERROR);
        vo.setCode(ResponseCodeEnum.CODE_500.getCode());
        vo.setInfo(ResponseCodeEnum.CODE_500.getMsg());
        vo.setData(t);
        return vo;
    }
    protected void readFile(HttpServletResponse response,String filePath){
        if(!StringUtils.pathIsOk(filePath)){
            return;
        }
        OutputStream out=null;
        FileInputStream input=null;
        try {
            File file=new File(filePath);
            if(!file.exists()){
                return;
            }
            input=new FileInputStream(file);
            byte[] byteData=new byte[1024];
            out=response.getOutputStream();
            int len=0;
            while((len=input.read(byteData))!=-1){
                out.write(byteData,0,len);
            }
            out.flush();
        }catch (Exception e){
            logger.error("读取文件异常",e);
        }finally {
            if(out!=null){
                try {
                    out.close();
                }catch (IOException e){
                    logger.error("IO异常",e);
                }
            }
            if(input!=null){
                try {
                    input.close();
                }catch (IOException e){
                    logger.error("IO异常",e);
                }
            }
        }
    }
    protected SessionWebUserDto getUserInfoFromSession(HttpSession session){
        SessionWebUserDto sessionWebUserDto=(SessionWebUserDto) session.getAttribute(Constants.session_key);
        return sessionWebUserDto;
    }
}
