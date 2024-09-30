package com.mypan.controller;

import com.mypan.enums.ResponseCodeEnum;

import com.mypan.entity.vo.ResponseVO;

public class ABaseController{
    protected static final String STATUC_SUCCESS="success";
    protected static final String STATUC_ERROR="error";
    protected <T> ResponseVO getSuccessResponseVO(T t){
        ResponseVO<T> responseVO =new ResponseVO<>();
        responseVO.setStatus(STATUC_SUCCESS);
        responseVO.setCode(ResponseCodeEnum.CODE_200.getCode());
        responseVO.setInfo(ResponseCodeEnum.CODE_200.getMsg());
        responseVO.setData(t);
        return responseVO;
    }
}
