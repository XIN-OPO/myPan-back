package com.mypan.service.Impl;
import com.mypan.component.RedisComponent;
import com.mypan.entity.constants.Constants;
import com.mypan.entity.dto.SessionWebUserDto;
import com.mypan.entity.dto.UploadResultDto;
import com.mypan.entity.dto.UserSpaceDto;
import com.mypan.entity.po.UserInfo;
import com.mypan.entity.query.UserInfoQuery;
import com.mypan.entity.vo.PaginationResultVO;
import com.mypan.entity.query.SimplePage;
import com.mypan.enums.*;
import com.mypan.entity.po.FileInfo;
import com.mypan.entity.query.FileInfoQuery;
import javax.annotation.Resource;

import com.mypan.exception.BusinessException;
import com.mypan.mappers.FileInfoMapper;
import com.mypan.mappers.UserInfoMapper;
import com.mypan.service.FileInfoService;
import com.mypan.utils.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

/**
 * @Description: 文件信息表ServiceImpl
 * @Author: 张鑫
 * @Date: 2024/10/06
*/
@Service("fileInfoService")
public class FileInfoServiceImpl implements FileInfoService {

	@Resource
	private FileInfoMapper<FileInfo,FileInfoQuery> fileInfoMapper;

	@Resource
	private RedisComponent redisComponent;

	@Resource
	private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

/**
 *根据条件查询列表
*/
	public List<FileInfo> findListByParam(FileInfoQuery query) {
		return this.fileInfoMapper.selectList(query);
	}
/**
 *根据条件查询数量
*/
	public Integer findCountByParam(FileInfoQuery query) {
		return this.fileInfoMapper.selectCount(query);
	}
/**
 *分页查询
*/
	public PaginationResultVO<FileInfo> findListByPage(FileInfoQuery query) {
		Integer count=this.findCountByParam(query);
		Integer pageSize=query.getPageSize()==null?PageSize.SIZE15.getSize():query.getPageSize();
		SimplePage page=new SimplePage(query.getPageNo(),count,pageSize);
		query.setSimplePage(page);
		List<FileInfo> list=this.findListByParam(query);
		PaginationResultVO<FileInfo> result=new PaginationResultVO(count,page.getPageSize(),page.getPageNo(),page.getPageTotal(),list);
		return result;
	}
/**
 *新增
*/
	public Integer add(FileInfo bean) {
		return this.fileInfoMapper.insert(bean);
	}
/**
 *批量新增
*/
	public Integer addBatch(List<FileInfo> listBean) {
		if (listBean==null || listBean.isEmpty()) {
			return 0;
		}
		return this.fileInfoMapper.insertBatch(listBean);
	}
/**
 *批量新增或修改
*/
	public Integer addOrUpdateBatch(List<FileInfo> listBean) {
		if (listBean==null || listBean.isEmpty()) {
			return 0;
		}
		return this.fileInfoMapper.insertOrUpdateBatch(listBean);
	}

/**
 *根据FileIdAndUserId查询
*/
	public FileInfo getFileInfoByFileIdAndUserId(String fileId, String userId) {
		return this.fileInfoMapper.selectByFileIdAndUserId(fileId, userId);
	}
/**
 *根据FileIdAndUserId更新
*/
	public Integer updateFileInfoByFileIdAndUserId( FileInfo bean , String fileId, String userId) {
		return this.fileInfoMapper.updateByFileIdAndUserId(bean,fileId, userId);
	}

/**
 *根据FileIdAndUserId删除
*/
	public Integer deleteFileInfoByFileIdAndUserId(String fileId, String userId) {
		return this.fileInfoMapper.deleteByFileIdAndUserId(fileId, userId);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public UploadResultDto uploadFile(SessionWebUserDto webUserDto, String fileId, MultipartFile file,
									  String fileName, String filePid, String fileMd5,
									  Integer chunkIndex, Integer chunks) throws BusinessException {
		UploadResultDto resultDto=new UploadResultDto();
		if(StringUtils.isEmpty(fileId)){
			fileId=StringUtils.getRandomNumber(Constants.length_10);
		}
		Date curDate=new Date();
		UserSpaceDto userSpaceDto= redisComponent.getUserSpaceUse(webUserDto.getUserId());
		if(chunkIndex==0){//第一个分片
			FileInfoQuery infoQuery=new FileInfoQuery();
			infoQuery.setFileMd5(fileMd5);
			infoQuery.setSimplePage(new SimplePage(0,1));
			infoQuery.setStatus(FileStatusEnums.USING.getStatus());
			List<FileInfo> dbFileList=this.fileInfoMapper.selectList(infoQuery);
			if(!dbFileList.isEmpty()){//秒传
				FileInfo dbFile=dbFileList.get(0);
				//判断文件大小
				if(dbFile.getFileSize()+userSpaceDto.getUseSpace()>userSpaceDto.getTotalSpace()){
					throw new BusinessException(ResponseCodeEnum.CODE_904);
				}
				dbFile.setFileId(fileId);
				dbFile.setFilePid(filePid);
				dbFile.setUserId(webUserDto.getUserId());
				dbFile.setCreateTime(curDate);
				dbFile.setLastUpdateTime(curDate);
				dbFile.setStatus(FileStatusEnums.USING.getStatus());
				dbFile.setDelFlag(FileDelFlag.USING.getFlag());
				dbFile.setFileMd5(fileMd5);
				//文件重命名
				fileName=autoRename(filePid, webUserDto.getUserId(), fileName);
				dbFile.setFileName(fileName);
				this.fileInfoMapper.insert(dbFile);
				resultDto.setStatus(UploadStatusEnums.UPLOAD_SECONDS.getCode());
				//更新用户使用空间
				updateUserSpace(webUserDto,dbFile.getFileSize());
				return resultDto;
			}
		}
		resultDto.setFileId(fileId);
		return resultDto;
	}

	private String autoRename(String filePid,String userId,String fileName){
		FileInfoQuery fileInfoQuery=new FileInfoQuery();
		fileInfoQuery.setUserId(userId);
		fileInfoQuery.setFilePid(filePid);
		fileInfoQuery.setDelFlag(FileDelFlag.USING.getFlag());
		fileInfoQuery.setFileName(fileName);
		Integer count=this.fileInfoMapper.selectCount(fileInfoQuery);
		if(count>0){
			fileName=StringUtils.rename(fileName);
		}
		return fileName;
	}
	private void updateUserSpace(SessionWebUserDto webUserDto,Long useSpace){
		Integer count=userInfoMapper.updateUserSpace(webUserDto.getUserId(),useSpace,null);
		if(count==0){
			try {
				throw new BusinessException(ResponseCodeEnum.CODE_904);
			} catch (BusinessException e) {
				e.printStackTrace();
			}
		}
		UserSpaceDto spaceDto=redisComponent.getUserSpaceUse(webUserDto.getUserId());
		spaceDto.setUseSpace(spaceDto.getUseSpace()+useSpace);
		redisComponent.saveUserSpaceUse(webUserDto.getUserId(), spaceDto);
	}
}




















