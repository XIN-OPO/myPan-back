package com.mypan.service;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.mypan.entity.dto.SessionWebUserDto;
import org.springframework.format.annotation.DateTimeFormat;
import com.mypan.enums.DateTimePatternEnum;
import com.mypan.utils.DateUtils;

import com.mypan.entity.vo.PaginationResultVO;
import com.mypan.entity.po.UserInfo;
import com.mypan.entity.query.UserInfoQuery;
import java.util.List;

/**
 * @Description: Service
 * @Author: 张鑫
 * @Date: 2024/09/30
*/
public interface UserInfoService{

/**
 *根据条件查询列表
*/
	List<UserInfo> findListByParam(UserInfoQuery query);

/**
 *根据条件查询数量
*/
	Integer findCountByParam(UserInfoQuery query);

/**
 *分页查询
*/
	PaginationResultVO<UserInfo> findListByPage(UserInfoQuery query);

/**
 *新增
*/
	Integer add(UserInfo bean);

/**
 *批量新增
*/
	Integer addBatch(List<UserInfo> listBean);

/**
 *批量新增或修改
*/
	Integer addOrUpdateBatch(List<UserInfo> listBean);


/**
 *根据UserId查询
*/
	UserInfo getUserInfoByUserId(String userId);

/**
 *根据UserId更新
*/
	Integer updateUserInfoByUserId( UserInfo bean , String userId);

/**
 *根据UserId删除
*/
	Integer deleteUserInfoByUserId(String userId);

/**
 *根据Email查询
*/
	UserInfo getUserInfoByEmail(String email);

/**
 *根据Email更新
*/
	Integer updateUserInfoByEmail( UserInfo bean , String email);

/**
 *根据Email删除
*/
	Integer deleteUserInfoByEmail(String email);

/**
 *根据QqOpenId查询
*/
	UserInfo getUserInfoByQqOpenId(String qqOpenId);

/**
 *根据QqOpenId更新
*/
	Integer updateUserInfoByQqOpenId( UserInfo bean , String qqOpenId);

/**
 *根据QqOpenId删除
*/
	Integer deleteUserInfoByQqOpenId(String qqOpenId);

/**
 *根据NickName查询
*/
	UserInfo getUserInfoByNickName(String nickName);

/**
 *根据NickName更新
*/
	Integer updateUserInfoByNickName( UserInfo bean , String nickName);

/**
 *根据NickName删除
*/
	Integer deleteUserInfoByNickName(String nickName);

	//注册
	void register(String email,String nickName,String password,String emailCode);

	//登录
	SessionWebUserDto login(String email ,String password);

	//重置密码
	void resetPwd(String email,String password,String emailCode);

	SessionWebUserDto qqLogin(String code);
}
