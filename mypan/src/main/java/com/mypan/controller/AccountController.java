package com.mypan.controller;

import com.mypan.entity.constants.Constants;
import com.mypan.entity.dto.CreateImageCode;
import com.mypan.entity.po.UserInfo;
import com.mypan.entity.query.UserInfoQuery;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.mypan.exception.BusinessException;
import com.mypan.service.EmailCodeService;
import com.mypan.service.UserInfoService;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.mypan.entity.vo.ResponseVO;

import java.io.IOException;
import java.util.List;

/**
 * @Description: Controller
 * @Author: 张鑫
 * @Date: 2024/09/30
*/
@RestController("userInfoController")
public class AccountController extends ABaseController {

	@Resource
	private UserInfoService userInfoService;

	@Resource
	private EmailCodeService emailCodeService;

	@RequestMapping("/checkCode")
	public void checkCode(HttpServletResponse response, HttpSession session,Integer type) throws IOException {
		CreateImageCode vCode=new CreateImageCode(130,38,5,10);
		response.setHeader("Pragma","no-cache");
		response.setHeader("Cache-Control","no-cache");
		response.setDateHeader("Expires",0);
		response.setContentType("image/jpeg");
		String code=vCode.getCode();
		if(type==null || type==0){
			session.setAttribute(Constants.check_code_key,code);
		}else{
			session.setAttribute(Constants.check_code_key_email,code);
		}
		vCode.write(response.getOutputStream());
	}
	@RequestMapping("/sendEmailCode")
	public ResponseVO sendEmailCode(HttpSession session,String email,String checkCode,Integer type) throws BusinessException {
		try {
			if(!checkCode.equalsIgnoreCase((String)session.getAttribute(Constants.check_code_key_email))){
				throw new BusinessException("图片验证码不正确");
			}
			emailCodeService.sendEmailCode(email,type);
			return getSuccessResponseVO(null);
		}finally {
			session.removeAttribute(Constants.check_code_key_email);
		}
	}
}
