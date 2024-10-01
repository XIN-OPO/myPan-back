package com.mypan.entity.po;
import java.io.Serializable;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import com.mypan.enums.DateTimePatternEnum;
import com.mypan.utils.DateUtils;

public class EmailCode implements Serializable {
/**
 * @Description: 邮箱验证码
 * @Author: 张鑫
 * @Date: 2024/09/30
*/
/**
 *邮箱
*/
	private String email;

/**
 *编号
*/
	private String code;

/**
 *创建时间
*/
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date createTime;

/**
 *0:未使用 1:已经使用
*/
	private Integer status;

	 public void setEmail(String email){
		this.email=email;
	 }
	 public String getEmail(){
		return this.email;
	 }
	 public void setCode(String code){
		this.code=code;
	 }
	 public String getCode(){
		return this.code;
	 }
	 public void setCreateTime(Date createTime){
		this.createTime=createTime;
	 }
	 public Date getCreateTime(){
		return this.createTime;
	 }
	 public void setStatus(Integer status){
		this.status=status;
	 }
	 public Integer getStatus(){
		return this.status;
	 }
	@Override
	 public String toString(){
		return "email:"+(email == null ? "空" : email)+",code:"+(code == null ? "空" : code)+",create_time:"+(DateUtils.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()) == null ? "空" : createTime)+",status:"+(status == null ? "空" : status);
	}
}