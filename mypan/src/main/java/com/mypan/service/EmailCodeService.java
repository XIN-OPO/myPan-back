package com.mypan.service;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import com.mypan.enums.DateTimePatternEnum;
import com.mypan.utils.DateUtils;

import com.mypan.entity.vo.PaginationResultVO;
import com.mypan.entity.po.EmailCode;
import com.mypan.entity.query.EmailCodeQuery;
import java.util.List;

/**
 * @Description: 邮箱验证码Service
 * @Author: 张鑫
 * @Date: 2024/09/30
*/
public interface EmailCodeService{

/**
 *根据条件查询列表
*/
	List<EmailCode> findListByParam(EmailCodeQuery query);

/**
 *根据条件查询数量
*/
	Integer findCountByParam(EmailCodeQuery query);

/**
 *分页查询
*/
	PaginationResultVO<EmailCode> findListByPage(EmailCodeQuery query);

/**
 *新增
*/
	Integer add(EmailCode bean);

/**
 *批量新增
*/
	Integer addBatch(List<EmailCode> listBean);

/**
 *批量新增或修改
*/
	Integer addOrUpdateBatch(List<EmailCode> listBean);


/**
 *根据EmailAndCode查询
*/
	EmailCode getEmailCodeByEmailAndCode(String email, String code);

/**
 *根据EmailAndCode更新
*/
	Integer updateEmailCodeByEmailAndCode( EmailCode bean , String email, String code);

/**
 *根据EmailAndCode删除
*/
	Integer deleteEmailCodeByEmailAndCode(String email, String code);

	void sendEmailCode(String email,Integer type);

	void checkCode(String email,String code);
}
