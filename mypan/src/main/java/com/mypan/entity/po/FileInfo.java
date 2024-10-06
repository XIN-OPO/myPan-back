package com.mypan.entity.po;
import java.io.Serializable;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import com.mypan.enums.DateTimePatternEnum;
import com.mypan.utils.DateUtils;

public class FileInfo implements Serializable {
/**
 * @Description: 文件信息表
 * @Author: 张鑫
 * @Date: 2024/10/06
*/
/**
 *文件id
*/
	private String fileId;

/**
 *用户id
*/
	private String userId;

/**
 *文件md5值
*/
	private String fileMd5;

/**
 *父级id
*/
	private String filePid;

/**
 *文件大小
*/
	private Long fileSize;

/**
 *文件名
*/
	private String fileName;

/**
 *封面
*/
	private String fileCover;

/**
 *文件路径
*/
	private String filePath;

/**
 *创建时间
*/
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date createTime;

/**
 *更新时间
*/
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date lastUpdateTime;

/**
 *0:文件 1：目录
*/
	private Integer folderType;

/**
 *文件分类 1:视频 2：音频 3：图片 4：文档 5：其他
*/
	private Integer fileCategary;

/**
 *1:视频 2：音频 3：图片 4：pdf 5：doc 6：excel 7：txt 8：code 9：zip 10：其他
*/
	private Integer fileType;

/**
 *0:转码中 1：转码失败 2：转码成功
*/
	private Integer status;

/**
 *进入回收站时间
*/
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date recoveryTime;

/**
 *标记删除 0：删除 1：回收站 2：正常
*/
	private Integer delFlag;

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
	 public void setFileMd5(String fileMd5){
		this.fileMd5=fileMd5;
	 }
	 public String getFileMd5(){
		return this.fileMd5;
	 }
	 public void setFilePid(String filePid){
		this.filePid=filePid;
	 }
	 public String getFilePid(){
		return this.filePid;
	 }
	 public void setFileSize(Long fileSize){
		this.fileSize=fileSize;
	 }
	 public Long getFileSize(){
		return this.fileSize;
	 }
	 public void setFileName(String fileName){
		this.fileName=fileName;
	 }
	 public String getFileName(){
		return this.fileName;
	 }
	 public void setFileCover(String fileCover){
		this.fileCover=fileCover;
	 }
	 public String getFileCover(){
		return this.fileCover;
	 }
	 public void setFilePath(String filePath){
		this.filePath=filePath;
	 }
	 public String getFilePath(){
		return this.filePath;
	 }
	 public void setCreateTime(Date createTime){
		this.createTime=createTime;
	 }
	 public Date getCreateTime(){
		return this.createTime;
	 }
	 public void setLastUpdateTime(Date lastUpdateTime){
		this.lastUpdateTime=lastUpdateTime;
	 }
	 public Date getLastUpdateTime(){
		return this.lastUpdateTime;
	 }
	 public void setFolderType(Integer folderType){
		this.folderType=folderType;
	 }
	 public Integer getFolderType(){
		return this.folderType;
	 }
	 public void setFileCategary(Integer fileCategary){
		this.fileCategary=fileCategary;
	 }
	 public Integer getFileCategary(){
		return this.fileCategary;
	 }
	 public void setFileType(Integer fileType){
		this.fileType=fileType;
	 }
	 public Integer getFileType(){
		return this.fileType;
	 }
	 public void setStatus(Integer status){
		this.status=status;
	 }
	 public Integer getStatus(){
		return this.status;
	 }
	 public void setRecoveryTime(Date recoveryTime){
		this.recoveryTime=recoveryTime;
	 }
	 public Date getRecoveryTime(){
		return this.recoveryTime;
	 }
	 public void setDelFlag(Integer delFlag){
		this.delFlag=delFlag;
	 }
	 public Integer getDelFlag(){
		return this.delFlag;
	 }
	@Override
	 public String toString(){
		return "file_id:"+(fileId == null ? "空" : fileId)+",user_id:"+(userId == null ? "空" : userId)+",file_md5:"+(fileMd5 == null ? "空" : fileMd5)+",file_pid:"+(filePid == null ? "空" : filePid)+",file_size:"+(fileSize == null ? "空" : fileSize)+",file_name:"+(fileName == null ? "空" : fileName)+",file_cover:"+(fileCover == null ? "空" : fileCover)+",file_path:"+(filePath == null ? "空" : filePath)+",create_time:"+(DateUtils.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()) == null ? "空" : createTime)+",last_update_time:"+(DateUtils.format(lastUpdateTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()) == null ? "空" : lastUpdateTime)+",folder_type:"+(folderType == null ? "空" : folderType)+",file_categary:"+(fileCategary == null ? "空" : fileCategary)+",file_type:"+(fileType == null ? "空" : fileType)+",status:"+(status == null ? "空" : status)+",recovery_time:"+(DateUtils.format(recoveryTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()) == null ? "空" : recoveryTime)+",del_flag:"+(delFlag == null ? "空" : delFlag);
	}
}