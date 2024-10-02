package com.mypan.service.Impl;
import com.mypan.component.RedisComponent;
import com.mypan.entity.config.AppConfig;
import com.mypan.entity.constants.Constants;
import com.mypan.entity.dto.SysSettingsDto;
import com.mypan.entity.po.UserInfo;
import com.mypan.entity.query.UserInfoQuery;
import com.mypan.entity.vo.PaginationResultVO;
import com.mypan.entity.query.SimplePage;
import com.mypan.enums.PageSize;
import com.mypan.entity.po.EmailCode;
import com.mypan.entity.query.EmailCodeQuery;
import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import com.mypan.exception.BusinessException;
import com.mypan.mappers.EmailCodeMapper;
import com.mypan.mappers.UserInfoMapper;
import com.mypan.service.EmailCodeService;
import com.mypan.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * @Description: 邮箱验证码ServiceImpl
 * @Author: 张鑫
 * @Date: 2024/09/30
*/
@Service("emailCodeService")
public class EmailCodeServiceImpl implements EmailCodeService {

	private static final Logger logger= LoggerFactory.getLogger(EmailCodeServiceImpl.class);

	@Resource
	private EmailCodeMapper<EmailCode,EmailCodeQuery> emailCodeMapper;

	@Resource
	private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

	@Resource
	private JavaMailSender javaMailSender;

	@Resource
	private AppConfig appConfig;

	@Resource
	private RedisComponent redisComponent;
/**
 *根据条件查询列表
*/
	public List<EmailCode> findListByParam(EmailCodeQuery query) {
		return this.emailCodeMapper.selectList(query);
	}
/**
 *根据条件查询数量
*/
	public Integer findCountByParam(EmailCodeQuery query) {
		return this.emailCodeMapper.selectCount(query);
	}
/**
 *分页查询
*/
	public PaginationResultVO<EmailCode> findListByPage(EmailCodeQuery query) {
		Integer count=this.findCountByParam(query);
		Integer pageSize=query.getPageSize()==null?PageSize.SIZE15.getSize():query.getPageSize();
		SimplePage page=new SimplePage(query.getPageNo(),count,pageSize);
		query.setSimplePage(page);
		List<EmailCode> list=this.findListByParam(query);
		PaginationResultVO<EmailCode> result=new PaginationResultVO(count,page.getPageSize(),page.getPageNo(),page.getPageTotal(),list);
		return result;
	}
/**
 *新增
*/
	public Integer add(EmailCode bean) {
		return this.emailCodeMapper.insert(bean);
	}
/**
 *批量新增
*/
	public Integer addBatch(List<EmailCode> listBean) {
		if (listBean==null || listBean.isEmpty()) {
			return 0;
		}
		return this.emailCodeMapper.insertBatch(listBean);
	}
/**
 *批量新增或修改
*/
	public Integer addOrUpdateBatch(List<EmailCode> listBean) {
		if (listBean==null || listBean.isEmpty()) {
			return 0;
		}
		return this.emailCodeMapper.insertOrUpdateBatch(listBean);
	}

/**
 *根据EmailAndCode查询
*/
	public EmailCode getEmailCodeByEmailAndCode(String email, String code) {
		return this.emailCodeMapper.selectByEmailAndCode(email, code);
	}
/**
 *根据EmailAndCode更新
*/
	public Integer updateEmailCodeByEmailAndCode( EmailCode bean , String email, String code) {
		return this.emailCodeMapper.updateByEmailAndCode(bean,email, code);
	}

/**
 *根据EmailAndCode删除
*/
	public Integer deleteEmailCodeByEmailAndCode(String email, String code) {
		return this.emailCodeMapper.deleteByEmailAndCode(email, code);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void sendEmailCode(String email, Integer type) {
		if(type==Constants.zero){
			UserInfo userInfo=userInfoMapper.selectByEmail(email);
			if(null!=userInfo){
				try {
					throw new BusinessException("邮箱已经存在");
				} catch (BusinessException e) {
					e.printStackTrace();
				}
			}
		}
		String code= StringUtils.getRandomNumber(Constants.length_5);
		// 发送验证码
		sendMailCode(email,code);

		//将之前的验证码置为无效
		emailCodeMapper.disableEmailCode(email);

		EmailCode emailCode=new EmailCode();
		emailCode.setCode(code);
		emailCode.setEmail(email);
		emailCode.setStatus(Constants.zero);
		emailCode.setCreateTime(new Date());
		emailCodeMapper.insert(emailCode);
	}
	private void sendMailCode(String toEmail,String code) {
		try {
			MimeMessage message=javaMailSender.createMimeMessage();
			MimeMessageHelper helper=new MimeMessageHelper(message,true);
			helper.setFrom(appConfig.getSendUserName());
			helper.setTo(toEmail);
			SysSettingsDto sysSettingsDto=redisComponent.getSysSettingDto();
			helper.setSubject(sysSettingsDto.getRegisterMailTitle());
			helper.setText(String.format(sysSettingsDto.getRegisterEmailContent(),code));
			helper.setSentDate(new Date());
			javaMailSender.send(message);
		} catch (MessagingException e) {
			logger.error("邮件发送失败",e);
			try {
				throw new BusinessException("邮件发送失败");
			} catch (BusinessException ex) {
				ex.printStackTrace();
			}
		}
	}

	@Override
	public void checkCode(String email, String code) {
		EmailCode emailCode=this.emailCodeMapper.selectByEmailAndCode(email,code);
		if(null==emailCode){
			try {
				throw new BusinessException("邮箱验证码不正确");
			} catch (BusinessException e) {
				e.printStackTrace();
			}
		}
		if(emailCode.getStatus()==1 || System.currentTimeMillis()-emailCode.getCreateTime().getTime()>Constants.length_15*1000*60){
			throw new RuntimeException("邮箱验证码已失效");
		}
		emailCodeMapper.disableEmailCode(email);
	}
}
