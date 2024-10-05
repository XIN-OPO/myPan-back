package com.mypan.service.Impl;
import com.mypan.component.RedisComponent;
import com.mypan.entity.config.AppConfig;
import com.mypan.entity.constants.Constants;
import com.mypan.entity.dto.QQInfoDto;
import com.mypan.entity.dto.SessionWebUserDto;
import com.mypan.entity.dto.SysSettingsDto;
import com.mypan.entity.dto.UserSpaceDto;
import com.mypan.entity.vo.PaginationResultVO;
import com.mypan.entity.query.SimplePage;
import com.mypan.enums.PageSize;
import com.mypan.entity.po.UserInfo;
import com.mypan.entity.query.UserInfoQuery;
import javax.annotation.Resource;

import com.mypan.enums.UserStatusEnum;
import com.mypan.exception.BusinessException;
import com.mypan.mappers.UserInfoMapper;
import com.mypan.service.EmailCodeService;
import com.mypan.service.UserInfoService;
import com.mypan.utils.JsonUtils;
import com.mypan.utils.OKHttpUtils;
import com.mypan.utils.StringUtils;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * @Description: ServiceImpl
 * @Author: 张鑫
 * @Date: 2024/09/30
*/
@Service("userInfoService")
public class UserInfoServiceImpl implements UserInfoService {

	private static Logger logger= LoggerFactory.getLogger(UserInfoServiceImpl.class);

	@Resource
	private UserInfoMapper<UserInfo,UserInfoQuery> userInfoMapper;

	@Resource
	private EmailCodeService emailCodeService;

	@Resource
	private RedisComponent redisComponent;

	@Resource
	private AppConfig appConfig;

/**
 *根据条件查询列表
*/
	public List<UserInfo> findListByParam(UserInfoQuery query) {
		return this.userInfoMapper.selectList(query);
	}
/**
 *根据条件查询数量
*/
	public Integer findCountByParam(UserInfoQuery query) {
		return this.userInfoMapper.selectCount(query);
	}
/**
 *分页查询
*/
	public PaginationResultVO<UserInfo> findListByPage(UserInfoQuery query) {
		Integer count=this.findCountByParam(query);
		Integer pageSize=query.getPageSize()==null?PageSize.SIZE15.getSize():query.getPageSize();
		SimplePage page=new SimplePage(query.getPageNo(),count,pageSize);
		query.setSimplePage(page);
		List<UserInfo> list=this.findListByParam(query);
		PaginationResultVO<UserInfo> result=new PaginationResultVO(count,page.getPageSize(),page.getPageNo(),page.getPageTotal(),list);
		return result;
	}
/**
 *新增
*/
	public Integer add(UserInfo bean) {
		return this.userInfoMapper.insert(bean);
	}
/**
 *批量新增
*/
	public Integer addBatch(List<UserInfo> listBean) {
		if (listBean==null || listBean.isEmpty()) {
			return 0;
		}
		return this.userInfoMapper.insertBatch(listBean);
	}
/**
 *批量新增或修改
*/
	public Integer addOrUpdateBatch(List<UserInfo> listBean) {
		if (listBean==null || listBean.isEmpty()) {
			return 0;
		}
		return this.userInfoMapper.insertOrUpdateBatch(listBean);
	}

/**
 *根据UserId查询
*/
	public UserInfo getUserInfoByUserId(String userId) {
		return this.userInfoMapper.selectByUserId(userId);
	}
/**
 *根据UserId更新
*/
	public Integer updateUserInfoByUserId( UserInfo bean , String userId) {
		return this.userInfoMapper.updateByUserId(bean,userId);
	}

/**
 *根据UserId删除
*/
	public Integer deleteUserInfoByUserId(String userId) {
		return this.userInfoMapper.deleteByUserId(userId);
	}

/**
 *根据Email查询
*/
	public UserInfo getUserInfoByEmail(String email) {
		return this.userInfoMapper.selectByEmail(email);
	}
/**
 *根据Email更新
*/
	public Integer updateUserInfoByEmail( UserInfo bean , String email) {
		return this.userInfoMapper.updateByEmail(bean,email);
	}

/**
 *根据Email删除
*/
	public Integer deleteUserInfoByEmail(String email) {
		return this.userInfoMapper.deleteByEmail(email);
	}

/**
 *根据QqOpenId查询
*/
	public UserInfo getUserInfoByQqOpenId(String qqOpenId) {
		return this.userInfoMapper.selectByQqOpenId(qqOpenId);
	}
/**
 *根据QqOpenId更新
*/
	public Integer updateUserInfoByQqOpenId( UserInfo bean , String qqOpenId) {
		return this.userInfoMapper.updateByQqOpenId(bean,qqOpenId);
	}

/**
 *根据QqOpenId删除
*/
	public Integer deleteUserInfoByQqOpenId(String qqOpenId) {
		return this.userInfoMapper.deleteByQqOpenId(qqOpenId);
	}

/**
 *根据NickName查询
*/
	public UserInfo getUserInfoByNickName(String nickName) {
		return this.userInfoMapper.selectByNickName(nickName);
	}
/**
 *根据NickName更新
*/
	public Integer updateUserInfoByNickName( UserInfo bean , String nickName) {
		return this.userInfoMapper.updateByNickName(bean,nickName);
	}

/**
 *根据NickName删除
*/
	public Integer deleteUserInfoByNickName(String nickName) {
		return this.userInfoMapper.deleteByNickName(nickName);
	}



	//实现注册
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void register(String email, String nickName, String password, String emailCode) {
		UserInfo userInfo=this.userInfoMapper.selectByEmail(email);
		if(null!=userInfo){
			try {
				throw new BusinessException("邮箱账号已经存在");
			} catch (BusinessException e) {
				e.printStackTrace();
			}
		}
		UserInfo nickNameUser=this.userInfoMapper.selectByNickName(nickName);
		if(null!=nickNameUser){
			try {
				throw new BusinessException("昵称已经存在");
			} catch (BusinessException e) {
				e.printStackTrace();
			}
		}
		//校验邮箱验证码
		emailCodeService.checkCode(email,emailCode);

		String userId= StringUtils.getRandomNumber(Constants.length_10);
		userInfo=new UserInfo();
		userInfo.setUserId(userId);
		userInfo.setNickName(nickName);
		userInfo.setEmail(email);
		userInfo.setPassword(StringUtils.encodeByMD5(password));//进行md5存储
		userInfo.setJoinTime(new Date());
		userInfo.setStatus(UserStatusEnum.ENABLE.getStatus());
		userInfo.setUseSpace(0L);

		SysSettingsDto sysSettingsDto=redisComponent.getSysSettingDto();

		userInfo.setTotalSpace(sysSettingsDto.getUserInitUseSpace()*Constants.MB);
		this.userInfoMapper.insert(userInfo);
	}

	@Override
	public SessionWebUserDto login(String email, String password)  {
		UserInfo userInfo=this.userInfoMapper.selectByEmail(email);
		if(null==userInfo || !userInfo.getPassword().equals(password)){
			try {
				throw new BusinessException("账号或密码错误");
			} catch (BusinessException e) {
				e.printStackTrace();
			}
		}
		if(UserStatusEnum.DISABLE.equals(userInfo.getStatus())){
			try {
				throw new BusinessException("账号已被封禁");
			} catch (BusinessException e) {
				e.printStackTrace();
			}
		}
		UserInfo updateInfo=new UserInfo();
		updateInfo.setLastLoginTime(new Date());
		this.userInfoMapper.updateByUserId(updateInfo,userInfo.getUserId());

		SessionWebUserDto sessionWebUserDto= new SessionWebUserDto();
		sessionWebUserDto.setNickName(userInfo.getNickName());
		sessionWebUserDto.setUserId(userInfo.getUserId());
		if(ArrayUtils.contains(appConfig.getAdminEmails().split(","),email)){
			sessionWebUserDto.setAdmin(true);
		}else {
			sessionWebUserDto.setAdmin(false);
	 	}
		//用户空间
		UserSpaceDto userSpaceDto=new UserSpaceDto();
		//TODO 查询当前用户已经上床文件大小的总和
		//userSpaceDto.setUseSpace();
		userSpaceDto.setTotalSpace(userInfo.getTotalSpace());
		redisComponent.saveUserSpaceUse(userInfo.getUserId(), userSpaceDto);
		return sessionWebUserDto;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void resetPwd(String email, String password, String emailCode) {
		UserInfo userInfo=this.userInfoMapper.selectByEmail(email);
		if(null==userInfo){
			try {
				throw new BusinessException("不存在该账号");
			} catch (BusinessException e) {
				e.printStackTrace();
			}
		}
		emailCodeService.checkCode(email,emailCode);
		UserInfo updateInfo=new UserInfo();
		updateInfo.setPassword(StringUtils.encodeByMD5(password));
		this.userInfoMapper.updateByEmail(updateInfo,email);
	}

	@Override
	public SessionWebUserDto qqLogin(String code){
		try {
			//第一步 通过回调code获取accessToken
			String accessToken=getQQAccessToken(code);
			//第二步 获取qqopenid

			String openId=getQQOpenId(code);
			UserInfo user=this.userInfoMapper.selectByUserId(openId);
			String avatar=null;
			if(null==user){
				//第三步 获取qq用户信息
				QQInfoDto qqInfoDto=getQQUserInfo(accessToken,openId);
				user=new UserInfo();
				String nickName=qqInfoDto.getNickName();
				nickName=nickName.length()>Constants.length_20?nickName.substring(0,Constants.length_20):nickName;
				avatar=StringUtils.isEmpty(qqInfoDto.getFigureurl_qq_2())?qqInfoDto.getFigureurl_qq_1():qqInfoDto.getFigureurl_qq_2();
				Date curDate=new Date();
				user.setQqOpenId(openId);
				user.setJoinTime(curDate);
				user.setNickName(nickName);
				user.setQqAvatar(avatar);
				user.setUserId(StringUtils.getRandomNumber(Constants.length_10));
				user.setLastLoginTime(curDate);
				user.setStatus(UserStatusEnum.ENABLE.getStatus());
				user.setUseSpace(0L);
				user.setTotalSpace(redisComponent.getSysSettingDto().getUserInitUseSpace()*Constants.MB);
				this.userInfoMapper.insert(user);
				user=userInfoMapper.selectByQqOpenId(openId);
			}else {
				UserInfo updateInfo=new UserInfo();
				updateInfo.setLastLoginTime(new Date());
				avatar= user.getQqAvatar();
				this.userInfoMapper.updateByQqOpenId(updateInfo,openId);
			}
			SessionWebUserDto sessionWebUserDto=new SessionWebUserDto();
			sessionWebUserDto.setUserId(user.getUserId());
			sessionWebUserDto.setAvatar(avatar);
			sessionWebUserDto.setNickName(user.getNickName());
			if(ArrayUtils.contains(appConfig.getAdminEmails().split(","),user.getEmail()==null?"":user.getEmail())){
				sessionWebUserDto.setAdmin(true);
			}else {
				sessionWebUserDto.setAdmin(false);
			}
			UserSpaceDto userSpaceDto=new UserSpaceDto();
			//TODO 获取用户已经使用的空间
			userSpaceDto.setUseSpace(0L);
			userSpaceDto.setTotalSpace(user.getTotalSpace());
			redisComponent.saveUserSpaceUse(user.getUserId(), userSpaceDto);
			return sessionWebUserDto;
		} catch (BusinessException e) {
			e.printStackTrace();
		}
		return null;
	}
	private String getQQAccessToken(String code) throws BusinessException {
		String accessToken=null;
		String url=null;
		try{
			url=String.format(appConfig.getQqUrlAccessToken(),appConfig.getQqAppId(),appConfig.getQqAppKey(),code, URLEncoder.encode(appConfig.getQqUrlRedirect(),"utf-8"));

		}catch (UnsupportedEncodingException e){
			logger.error("encode失败",e);
		}

		String tokenResult= OKHttpUtils.getRequest(url);
		if(tokenResult==null || tokenResult.indexOf(Constants.view_obj_result_key)!=-1){
			logger.error("获取qqtoken失败：{}",tokenResult);
			throw new BusinessException("获取qqToken失败");
		}
		String[] params=tokenResult.split("&");
		if(params!=null && params.length>0){
			for(String p:params){
				if(p.indexOf("access_token")!=-1){
					accessToken=p.split("=")[-1];
					break;
				}
			}
		}
		return accessToken;
	}

	private String getQQOpenId(String accessToken) throws BusinessException{
		String url=String.format(appConfig.getQqUrlOpenid(),accessToken);
		String openIdResult=OKHttpUtils.getRequest(url);
		String tmpJson=this.getQQResp(openIdResult);
		if(tmpJson==null){
			logger.error("调取qq接口获取openid失败：tempJson{}",tmpJson);
			throw new BusinessException("调qq接口获取openid失败");
		}
		Map jsonData= JsonUtils.convertJson2Obj(tmpJson,Map.class);
		if(jsonData==null || jsonData.containsKey(Constants.view_obj_result_key)){
			logger.error("调qq接口获取openid失败:{}",jsonData);
			throw new BusinessException("调qq接口获取openid失败");
		}
		return String.valueOf(jsonData.get("openid"));
	}

	private String getQQResp(String result){
		if(org.apache.commons.lang3.StringUtils.isNotBlank(result)){
			int pos=result.indexOf("callback");
			if(pos!=-1){
				int start=result.indexOf("(");
				int end=result.lastIndexOf(")");
				String jsonStr=result.substring(start+1,end-1);
				return jsonStr;
			}
		}
		return null;
	}
	private QQInfoDto getQQUserInfo(String accessToken, String qqopenId) throws BusinessException{
		String url=String.format(appConfig.getQqUrlUserInfo(),accessToken,appConfig.getQqAppId(),qqopenId);
		String response=OKHttpUtils.getRequest(url);
		if(org.apache.commons.lang3.StringUtils.isNotBlank(response)){
			QQInfoDto qqInfoDto=JsonUtils.convertJson2Obj(response,QQInfoDto.class);
			if(qqInfoDto.getRet()!=0){
				logger.error("qqInfo:{}",response);
				throw new BusinessException("调qq接口获取用户信息异常");
			}
			return qqInfoDto;
		}
		throw new BusinessException("调qq接口获取用户信息异常");
	}
}
