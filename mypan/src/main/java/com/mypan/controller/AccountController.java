package com.mypan.controller;

import com.mypan.annotation.GlobalInterceptor;
import com.mypan.annotation.VerifyParam;
import com.mypan.component.RedisComponent;
import com.mypan.entity.config.AppConfig;
import com.mypan.entity.constants.Constants;
import com.mypan.entity.dto.CreateImageCode;
import com.mypan.entity.dto.SessionWebUserDto;
import com.mypan.entity.dto.UserSpaceDto;
import com.mypan.entity.po.UserInfo;
import com.mypan.entity.query.UserInfoQuery;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.mypan.enums.VerifyRegexEnum;
import com.mypan.exception.BusinessException;
import com.mypan.service.EmailCodeService;
import com.mypan.service.UserInfoService;
import com.mypan.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.mypan.entity.vo.ResponseVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: Controller
 * @Author: 张鑫
 * @Date: 2024/09/30
*/
@RestController("userInfoController")
public class AccountController extends ABaseController {
	private static Logger logger= LoggerFactory.getLogger(AccountController.class);
	private static final String CONTENT_TYPE="Content-Type";
	private static final String CONTENT_TYPE_VALUE="application/json;charset=UTF-8";

	@Resource
	private UserInfoService userInfoService;

	@Resource
	private EmailCodeService emailCodeService;

	@Resource
	private AppConfig appConfig;

	@Resource
	private RedisComponent redisComponent;

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
	@GlobalInterceptor(checkParams = true,checkLogin = false)
	public ResponseVO sendEmailCode(HttpSession session,@VerifyParam(required = true) String email,
									@VerifyParam(required = true) String checkCode,
									@VerifyParam(required = true) Integer type) throws BusinessException {
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

	@RequestMapping("/register")
	@GlobalInterceptor(checkParams = true,checkLogin = false)
	public ResponseVO register(HttpSession session,
							   @VerifyParam(required = true,regex = VerifyRegexEnum.EMAIL,max = 150) String email,
							   @VerifyParam(required = true) String nickName,
							   @VerifyParam(required = true,regex = VerifyRegexEnum.PASSWORD,min = 8,max = 18) String password,
							   @VerifyParam(required = true) String emailCode,
							   @VerifyParam(required = true) String checkCode) throws BusinessException {
		try {
			if(!checkCode.equalsIgnoreCase((String)session.getAttribute(Constants.check_code_key))){
				throw new BusinessException("图片验证码不正确");
			}
			userInfoService.register(email,nickName,password,emailCode);
			return getSuccessResponseVO(null);
		}finally {
			session.removeAttribute(Constants.check_code_key);
		}
	}

	@RequestMapping("/login")
	@GlobalInterceptor(checkParams = true,checkLogin = false)
	public ResponseVO login(HttpSession session,
							   @VerifyParam(required = true) String email,
							   @VerifyParam(required = true) String password,
							   @VerifyParam(required = true) String checkCode) throws BusinessException {
		try {
			if(!checkCode.equalsIgnoreCase((String)session.getAttribute(Constants.check_code_key))){
				throw new BusinessException("图片验证码不正确");
			}
			SessionWebUserDto sessionWebUserDto=userInfoService.login(email,password);
			session.setAttribute(Constants.session_key,sessionWebUserDto);
			return getSuccessResponseVO(sessionWebUserDto);
		}finally {
			session.removeAttribute(Constants.check_code_key);
		}
	}

	@RequestMapping("/resetPwd")
	@GlobalInterceptor(checkParams = true,checkLogin = false)
	public ResponseVO resetPwd(HttpSession session,
							   @VerifyParam(required = true,regex = VerifyRegexEnum.EMAIL,max = 150) String email,
							   @VerifyParam(required = true,regex = VerifyRegexEnum.PASSWORD,min = 8,max = 18) String password,
							   @VerifyParam(required = true) String emailCode,
							   @VerifyParam(required = true) String checkCode) throws BusinessException {
		try {
			if(!checkCode.equalsIgnoreCase((String)session.getAttribute(Constants.check_code_key))){
				throw new BusinessException("图片验证码不正确");
			}
			userInfoService.resetPwd(email,password,emailCode);

			return getSuccessResponseVO(null);
		}finally {
			session.removeAttribute(Constants.check_code_key);
		}
	}

	@RequestMapping("/getAvatar/{userId}")
	@GlobalInterceptor(checkParams = true,checkLogin = false)
	public void getAvatar(HttpServletResponse response,
							   @VerifyParam(required = true) @PathVariable("userId") String userId)
			throws BusinessException {
			String avatarFolderName=Constants.file_folder_file+Constants.file_folder_avatar_name;
			File fileFolder=new File(appConfig.getProjectFolder()+avatarFolderName);
			if(!fileFolder.exists()){
				fileFolder.mkdirs();
			}
			String avatarPath=appConfig.getProjectFolder()+avatarFolderName+userId+Constants.avatar_suffix;
			File file=new File(avatarPath);
			if(!file.exists()){
				if(!new File(appConfig.getProjectFolder()+avatarFolderName+Constants.default_avatar).exists()){
					printNoDefaultImage(response);
				}
				avatarPath=appConfig.getProjectFolder()+avatarFolderName+Constants.default_avatar;
			}
			response.setContentType("image/jpg");
			readFile(response,avatarPath);
	}
	private void printNoDefaultImage(HttpServletResponse response){
		response.setHeader(CONTENT_TYPE,CONTENT_TYPE_VALUE);
		response.setStatus(HttpStatus.OK.value());
		PrintWriter writer=null;
		try{
			writer=response.getWriter();
			writer.print("请在头像目录下放置默认头像default_avatar.jpg");
		}catch (Exception e){
			logger.error("输出无默认图片失败",e);
		}finally {
			writer.close();
		}
	}

	@RequestMapping("/getUserInfo")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO getUserInfo(HttpSession session)  {
		SessionWebUserDto sessionWebUserDto=getUserInfoFromSession(session);
		return getSuccessResponseVO(sessionWebUserDto);
	}

	@RequestMapping("/getUseSpace")
	@GlobalInterceptor
	public ResponseVO getUseSpace(HttpSession session)  {
		SessionWebUserDto sessionWebUserDto=getUserInfoFromSession(session);
		UserSpaceDto spaceDto= redisComponent.getUserSpaceUse(sessionWebUserDto.getUserId());
		return getSuccessResponseVO(spaceDto);
	}

	@RequestMapping("/logout")
	public ResponseVO logout(HttpSession session)  {
		session.invalidate();
		return getSuccessResponseVO(null);
	}

	@RequestMapping("/updateUserAvatar")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO updateUserAvatar(HttpSession session, MultipartFile avatar)  {
		SessionWebUserDto webUserDto=getUserInfoFromSession(session);
		String baseFolder= appConfig.getProjectFolder()+Constants.file_folder_file;
		File targetFileFolder=new File(baseFolder+Constants.file_folder_avatar_name);
		if(!targetFileFolder.exists()){
			targetFileFolder.mkdirs();
		}
		File targetFile=new File(targetFileFolder.getPath()+"/"+webUserDto.getUserId()+Constants.avatar_suffix);
		try {
			avatar.transferTo(targetFile);
		}catch (Exception e){
			logger.error("上传文件失败",e);
		}
		UserInfo userInfo=new UserInfo();
		userInfo.setQqAvatar("");
		userInfoService.updateUserInfoByUserId(userInfo,webUserDto.getUserId());
		webUserDto.setUserId(null);
		session.setAttribute(Constants.session_key,webUserDto);
		return getSuccessResponseVO(null);
	}

	@RequestMapping("/updatePassword")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO updatePassword(HttpSession session,
									 @VerifyParam(required = true,regex = VerifyRegexEnum.PASSWORD,min = 8,max = 18) String password)
	{
		SessionWebUserDto webUserDto=getUserInfoFromSession(session);
		UserInfo userInfo=new UserInfo();
		userInfo.setPassword(StringUtils.encodeByMD5(password));
		userInfoService.updateUserInfoByUserId(userInfo,webUserDto.getUserId());
		return getSuccessResponseVO(null);
	}

	@RequestMapping("/qqLogin")
	@GlobalInterceptor(checkParams = true,checkLogin = false)
	public ResponseVO qqLogin(HttpSession session,
									 String callbackUrl) throws UnsupportedEncodingException {
		String state=StringUtils.getRandomNumber(Constants.length_30);
		if(!StringUtils.isEmpty(callbackUrl)){
			session.setAttribute(state,callbackUrl);
		}
		String url=String.format(appConfig.getQqUrlAuthorization(),appConfig.getQqAppId(), URLEncoder.encode(appConfig.getQqUrlRedirect(),"utf-8"),state);
		return getSuccessResponseVO(url);
	}

	@RequestMapping("/qqLogin/callback")
	@GlobalInterceptor(checkParams = true,checkLogin = false)
	public ResponseVO qqLoginCallback(HttpSession session,
							  @VerifyParam(required = true) String code,
									  String state) throws BusinessException {
		SessionWebUserDto sessionWebUserDto=userInfoService.qqLogin(code);
		session.setAttribute(Constants.session_key,sessionWebUserDto);
		Map<String,Object> result=new HashMap<>();
		result.put("callbackUrl",session.getAttribute(state));
		result.put("userInfo",sessionWebUserDto);
		return getSuccessResponseVO(result);
	}
}
