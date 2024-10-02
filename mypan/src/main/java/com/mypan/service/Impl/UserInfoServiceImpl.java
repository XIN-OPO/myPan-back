package com.mypan.service.Impl;
import com.mypan.component.RedisComponent;
import com.mypan.entity.config.AppConfig;
import com.mypan.entity.constants.Constants;
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
import com.mypan.utils.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;


/**
 * @Description: ServiceImpl
 * @Author: 张鑫
 * @Date: 2024/09/30
*/
@Service("userInfoService")
public class UserInfoServiceImpl implements UserInfoService {

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
		//userSpaceDto.setUseSpace();
		userSpaceDto.setTotalSpace(userInfo.getTotalSpace());
		redisComponent.saveUserSpaceUse(userInfo.getUserId(), userSpaceDto);
		return null;
	}
}
