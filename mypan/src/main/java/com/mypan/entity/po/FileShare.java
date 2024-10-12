package com.mypan.entity.po;
import java.io.Serializable;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import com.mypan.enums.DateTimePatternEnum;
import com.mypan.utils.DateUtils;

public class FileShare implements Serializable {
/**
 * @Description: 分享信息
 * @Author: 张鑫
 * @Date: 2024/10/10
*/
	private String shareId;

	private String fileId;

	private String userId;

/**
 *有效期类型 0：1天 1：7天 2:30天 3：永久有效
*/
	private Integer validType;

/**
 *失效时间
*/
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date expireTime;

/**
 *分享时间
*/
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date shareTime;

/**
 *提取码
*/
	private String code;

/**
 *浏览次数
*/
	private Integer showCount;

	private String fileName;

	private String fileCover;

	private Integer folderType;

	public Integer getFileCategary() {
		return fileCategary;
	}

	public void setFileCategary(Integer fileCategary) {
		this.fileCategary = fileCategary;
	}

	private Integer fileCategary;

	private Integer fileType;

	public String getFileCover() {
		return fileCover;
	}

	public void setFileCover(String fileCover) {
		this.fileCover = fileCover;
	}

	public Integer getFolderType() {
		return folderType;
	}

	public void setFolderType(Integer folderType) {
		this.folderType = folderType;
	}


	public Integer getFileType() {
		return fileType;
	}

	public void setFileType(Integer fileType) {
		this.fileType = fileType;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public void setShareId(String shareId){
		this.shareId=shareId;
	 }
	 public String getShareId(){
		return this.shareId;
	 }
	 public void setFileId(String fileId){
		this.fileId=fileId;
	 }
	 public String getFileId(){
		return this.fileId;
	 }
	 public void setUserId(String userId){
		this.userId=userId;
	 }
	 public String getUserId(){
		return this.userId;
	 }
	 public void setValidType(Integer validType){
		this.validType=validType;
	 }
	 public Integer getValidType(){
		return this.validType;
	 }
	 public void setExpireTime(Date expireTime){
		this.expireTime=expireTime;
	 }
	 public Date getExpireTime(){
		return this.expireTime;
	 }
	 public void setShareTime(Date shareTime){
		this.shareTime=shareTime;
	 }
	 public Date getShareTime(){
		return this.shareTime;
	 }
	 public void setCode(String code){
		this.code=code;
	 }
	 public String getCode(){
		return this.code;
	 }
	 public void setShowCount(Integer showCount){
		this.showCount=showCount;
	 }
	 public Integer getShowCount(){
		return this.showCount;
	 }
	@Override
	 public String toString(){
		return "share_id:"+(shareId == null ? "空" : shareId)+",file_id:"+(fileId == null ? "空" : fileId)+",user_id:"+(userId == null ? "空" : userId)+",valid_type:"+(validType == null ? "空" : validType)+",expire_time:"+(DateUtils.format(expireTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()) == null ? "空" : expireTime)+",share_time:"+(DateUtils.format(shareTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()) == null ? "空" : shareTime)+",code:"+(code == null ? "空" : code)+",show_count:"+(showCount == null ? "空" : showCount);
	}
}
