package com.mypan.service.Impl;
import com.mypan.component.RedisComponent;
import com.mypan.entity.config.AppConfig;
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
import com.mypan.utils.DateUtils;
import com.mypan.utils.ProcessUtils;
import com.mypan.utils.ScaleFilter;
import com.mypan.utils.StringUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Description: 文件信息表ServiceImpl
 * @Author: 张鑫
 * @Date: 2024/10/06
*/
@Service("fileInfoService")
public class FileInfoServiceImpl implements FileInfoService {

	@Resource
	private FileInfoMapper<FileInfo,FileInfoQuery>  fileInfoMapper;

	@Resource
	private RedisComponent redisComponent;

	@Resource
	private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

	@Resource
	private AppConfig appConfig;

	@Resource
	@Lazy
	private FileInfoServiceImpl fileInfoServiceImp;

	private static final Logger logger= LoggerFactory.getLogger(FileInfoServiceImpl.class);

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

		Boolean uploadSuccess=true;
		File tempFileFolder=null;
		try {
			if(StringUtils.isEmpty(fileId)){
				fileId=StringUtils.getRandomString(Constants.length_10);
			}
			resultDto.setFileId(fileId);
			Date curDate=new Date();
			UserSpaceDto userSpaceDto= redisComponent.getUserSpaceUse(webUserDto.getUserId());

			if(chunkIndex==0) {//第一个分片
				FileInfoQuery infoQuery = new FileInfoQuery();
				infoQuery.setFileMd5(fileMd5);
				infoQuery.setSimplePage(new SimplePage(0, 1));
				infoQuery.setStatus(FileStatusEnums.USING.getStatus());
				List<FileInfo> dbFileList = fileInfoMapper.selectList(infoQuery);
				//秒传 判断是否已经有了md5
				if (!dbFileList.isEmpty()) {
					FileInfo dbFile = dbFileList.get(0);
					//判断文件大小
					if (dbFile.getFileSize() + userSpaceDto.getUseSpace() > userSpaceDto.getTotalSpace()) {
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
					fileName = autoRename(filePid, webUserDto.getUserId(), fileName);
					dbFile.setFileName(fileName);
					this.fileInfoMapper.insert(dbFile);
					resultDto.setStatus(UploadStatusEnums.UPLOAD_SECONDS.getCode());
					//更新用户使用空间
					updateUserSpace(webUserDto, dbFile.getFileSize());
					return resultDto;
				}
			}
			//判断磁盘空间
			Long currentTempSize=redisComponent.getFileTempSize(webUserDto.getUserId(), fileId);
			if(file.getSize()+currentTempSize+userSpaceDto.getUseSpace()>userSpaceDto.getTotalSpace()){
				throw new BusinessException(ResponseCodeEnum.CODE_904);
			}
			//正常的分片上传
			//暂存临时目录
			String tempFolderName=appConfig.getProjectFolder()+Constants.file_folder_temp;
			String currentUserFolderName= webUserDto.getUserId()+fileId;

			tempFileFolder=new File(tempFolderName,currentUserFolderName);
			if(!tempFileFolder.exists()){
				tempFileFolder.mkdirs();
			}
			File newFile=new File(tempFileFolder.getPath()+"/"+chunkIndex);
			file.transferTo(newFile);
			//保存临时大小
			redisComponent.saveTempFileSize(webUserDto.getUserId(), fileId,file.getSize());
			if(chunkIndex<chunks-1){
				resultDto.setStatus(UploadStatusEnums.UPLOADING.getCode());
				return resultDto;
			}
			redisComponent.saveTempFileSize(webUserDto.getUserId(), fileId,file.getSize());
			//最后一个分片上传完成，记入数据库，异步合并分片
			String month= DateUtils.format(new Date(),DateTimePatternEnum.YYYYMM.getPattern());
			String fileSuffix=StringUtils.getFileSuffix(fileName);
			String realFileName=currentUserFolderName+fileSuffix;
			FileTypeEnums fileTypeEnums=FileTypeEnums.getFileTypeBySuffix(fileSuffix);
			//自动重命名
			fileName=autoRename(filePid, webUserDto.getUserId(),fileName);
			FileInfo fileInfo=new FileInfo();
			fileInfo.setFileId(fileId);
			fileInfo.setUserId(webUserDto.getUserId());
			fileInfo.setFileMd5(fileMd5);
			fileInfo.setFileName(fileName);
			fileInfo.setFilePath(month+"/"+realFileName);
			fileInfo.setFilePid(filePid);
			fileInfo.setCreateTime(curDate);
			fileInfo.setLastUpdateTime(curDate);
			fileInfo.setFileCategary(fileTypeEnums.getCategory().getCategory());
			fileInfo.setFileType(fileTypeEnums.getType());
			fileInfo.setStatus(FileStatusEnums.TRANSFER.getStatus());
			fileInfo.setFolderType(FileFolderTypeEnums.FILE.getType());
			fileInfo.setDelFlag(FileDelFlag.USING.getFlag());
			this.fileInfoMapper.insert(fileInfo);
			Long totalSize= redisComponent.getFileTempSize(webUserDto.getUserId(), fileId);
			updateUserSpace(webUserDto,totalSize);

			resultDto.setStatus(UploadStatusEnums.UPLOAD_FINISH.getCode());

			//转码
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					try {
						fileInfoServiceImp.transferFile(fileInfo.getFileId(),webUserDto);
					} catch (BusinessException e) {
						e.printStackTrace();
					}
				}
			});


			return resultDto;
		}catch (BusinessException e){
			logger.error("文件上传失败",e);
			uploadSuccess=false;
			throw e;
		}catch (Exception e){
			logger.error("文件上传失败",e);
			uploadSuccess=false;
		}finally {
			if(!uploadSuccess && tempFileFolder!=null){
				try {
					FileUtils.deleteDirectory(tempFileFolder);
				} catch (IOException e) {
					logger.error("删除临时目录失败",e);
				}
			}
		}

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

	@Async
	public void transferFile(String fileId,SessionWebUserDto webUserDto) throws BusinessException{
		Boolean transferSuccess=true;
		String targetFilePath=null;
		String cover=null;
		FileTypeEnums fileTypeEnum=null;
		FileInfo fileInfo=this.fileInfoMapper.selectByFileIdAndUserId(fileId,webUserDto.getUserId());
		try {
			if(fileInfo==null || !FileStatusEnums.TRANSFER.getStatus().equals(fileInfo.getStatus())){
				return;
			}
			//临时目录
			String tempFolderName=appConfig.getProjectFolder()+Constants.file_folder_temp;
			String currentUserFolderName= webUserDto.getUserId()+fileId;
			File fileFolder=new File(tempFolderName+currentUserFolderName);

			String fileSuffix=StringUtils.getFileSuffix(fileInfo.getFileName());
			String month=DateUtils.format(fileInfo.getCreateTime(),DateTimePatternEnum.YYYYMM.getPattern());
			//目标目录
			String targetFolderName=appConfig.getProjectFolder()+Constants.file_folder_file;
			File targetFolder=new File(targetFolderName+"/"+month);
			if(!targetFolder.exists()){
				targetFolder.mkdirs();
			}
			//真实的文件名
			String realFileName=currentUserFolderName+fileSuffix;
			targetFilePath=targetFolder.getPath()+"/"+realFileName;

			//合并文件
			union(fileFolder.getPath(),targetFilePath,fileInfo.getFileName(),true);

			//视频文件的切割
			fileTypeEnum =FileTypeEnums.getFileTypeBySuffix(fileSuffix);
			if(FileTypeEnums.VIDEO==fileTypeEnum){
				cutFile4Video(fileId,targetFilePath);
				//视频生成缩略图
				cover=month+"/"+currentUserFolderName+Constants.image_png_suffix;
				String coverPath=targetFolderName+"/"+cover;
				ScaleFilter.createCover4Video(new File(targetFilePath),Constants.length_150,new File(coverPath));
			}else if(FileTypeEnums.IMAGE==fileTypeEnum){
				//生成缩略图
				cover=month+"/"+realFileName.replace(".","_.");
				String coverPath=targetFolderName+"/"+cover;
				Boolean created=ScaleFilter.createThumbnailWidthFFmpeg(new File(targetFilePath),Constants.length_150,new File(coverPath),false);
				if(!created){
					FileUtils.copyFile(new File(targetFilePath),new File(coverPath));
				}
			}
		}catch (Exception e){
			logger.error("文件转码失败，文件ID:{},UserId:{}",fileId,webUserDto.getUserId(),e);
			transferSuccess=false;
		}finally {
			FileInfo updateInfo=new FileInfo();
			updateInfo.setFileSize(new File(targetFilePath).length());
			updateInfo.setFileCover(cover);
			updateInfo.setStatus(transferSuccess?FileStatusEnums.USING.getStatus() : FileStatusEnums.TRANSFER_FALL.getStatus());
			fileInfoMapper.updateFileStatusWithOldStatus(fileId,webUserDto.getUserId(),updateInfo,FileStatusEnums.TRANSFER.getStatus());

		}
	}

	private void union(String dirPath,String toFilePath,String fileName,Boolean delSource) throws BusinessException {
		File dir=new File(dirPath);
		if(!dir.exists()){
			throw new BusinessException("目录不存在");
		}

		File[] fileList=dir.listFiles();
		File targetFile=new File(toFilePath);
		RandomAccessFile writeFile=null;
		try {
			writeFile=new RandomAccessFile(targetFile,"rw");
			byte[] b=new byte[1024*10];
			for(int i=0;i<fileList.length;i++){
				int len=-1;
				File chunkFile=new File(dirPath+"/"+i);
				RandomAccessFile readFile=null;
				try {
					readFile=new RandomAccessFile(chunkFile,"r");
					while ((len=readFile.read(b))!=-1){
						writeFile.write(b,0,len);
					}
				}catch (Exception e) {
					logger.error("合并分片失败",e);
					 throw new BusinessException("合并分片失败");
				}finally {
					readFile.close();
				}
			}
		}catch (Exception e){
			logger.error("合并文件{}失败",fileName,e);
			throw new BusinessException("合并文件"+fileName+"出错了");
		}finally {
			if(null!=writeFile){
				try {
					writeFile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(delSource && dir.exists()){
				try {
					FileUtils.deleteDirectory(dir);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	private void cutFile4Video(String fileId,String videoFilePath) throws BusinessException {
		//创建同名切片目录
		File tsFolder=new File(videoFilePath.substring(0,videoFilePath.lastIndexOf(".")));
		if(!tsFolder.exists()){
			tsFolder.mkdirs();
		}
		final String CMD_TRANSFER_2TS = "ffmpeg -y -i %s -vcodec copy -acodec copy -bsf:v h264_mp4toannexb %s";
		final String CMD_CUT_TS = "ffmpeg -i %s -c copy -map 0 -f segment -segment_list %s -segment_time 30 -reset_timestamps 1 %s/%s_%%04d.ts";
		String tsPath=tsFolder+"/"+Constants.ts_name;
		//生成.ts
		String cmd=String.format(CMD_TRANSFER_2TS,videoFilePath,tsPath);
		ProcessUtils.executeCommand(cmd,false);
		//生成索引文件.m3u8和切片.ts
		cmd=String.format(CMD_CUT_TS,tsPath,tsFolder.getPath()+"/"+Constants.m3u8_name,tsFolder.getPath(),fileId);
		ProcessUtils.executeCommand(cmd,false);
		//删除index.ts文件
		new File(tsPath).delete();
	}

	@Override
	public FileInfo newFolder(String filePid, String userId, String folderName) throws BusinessException {
		checkFileName(filePid,userId,folderName,FileFolderTypeEnums.FOLDER.getType());
		Date curDate=new Date();
		FileInfo fileInfo=new FileInfo();
		fileInfo.setFileId(StringUtils.getRandomString(Constants.length_10));
		fileInfo.setUserId(userId);
		fileInfo.setFilePid(filePid);
		fileInfo.setFileName(folderName);
		fileInfo.setFolderType(FileFolderTypeEnums.FOLDER.getType());
		fileInfo.setCreateTime(curDate);
		fileInfo.setLastUpdateTime(curDate);
		fileInfo.setStatus(FileStatusEnums.USING.getStatus());
		fileInfo.setDelFlag(FileDelFlag.USING.getFlag());
		this.fileInfoMapper.insert(fileInfo);
		return fileInfo;
	}
	private void checkFileName(String filePid,String userId,String fileName,Integer folderType) throws BusinessException {
		FileInfoQuery fileInfoQuery=new FileInfoQuery();
		fileInfoQuery.setFolderType(folderType);
		fileInfoQuery.setFileName(fileName);
		fileInfoQuery.setFilePid(filePid);
		fileInfoQuery.setUserId(userId);
		fileInfoQuery.setDelFlag(FileDelFlag.USING.getFlag());
		Integer count =this.fileInfoMapper.selectCount(fileInfoQuery);
		if(count>0){
			throw new BusinessException("此目录下已经存在同名文件，请修改名称");
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public FileInfo rename(String fileId, String userId, String fileName) throws BusinessException {
		FileInfo fileInfo=this.fileInfoMapper.selectByFileIdAndUserId(fileId,userId);
		if(null==fileInfo){
			throw new BusinessException("文件不存在");
		}
		String filePid=fileInfo.getFilePid();
		checkFileName(filePid,userId,fileName, fileInfo.getFolderType());
		//获取文件后缀
		if(FileFolderTypeEnums.FILE.getType().equals(fileInfo.getFileType())){
			fileName=fileName+StringUtils.getFileSuffix(fileInfo.getFileName());
		}
		Date curDate=new Date();
		FileInfo dbInfo=new FileInfo();
		//dbInfo.setFileId(fileId);
		dbInfo.setFileName(fileName);
		dbInfo.setLastUpdateTime(curDate);
		this.fileInfoMapper.updateByFileIdAndUserId(dbInfo,fileId,userId);
		FileInfoQuery fileInfoQuery=new FileInfoQuery();
		fileInfoQuery.setFilePid(filePid);
		fileInfoQuery.setUserId(userId);
		fileInfoQuery.setFileName(fileName);
		fileInfoQuery.setDelFlag(FileDelFlag.USING.getFlag());
		Integer count =this.fileInfoMapper.selectCount(fileInfoQuery);
		if(count>1){
			throw new BusinessException("文件名"+fileName+"已经存在");
		}
		fileInfo.setFileName(fileName);
		fileInfo.setLastUpdateTime(curDate);
		return fileInfo;
	}

	@Override
	public void changeFileFolder(String fileIds, String filePid, String userId) throws BusinessException {
		if(fileIds.equals(filePid)){
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		if(!Constants.zero_str.equals(filePid)){//如果不是在根目录
			FileInfo fileInfo=fileInfoServiceImp.getFileInfoByFileIdAndUserId(filePid,userId);
			if(fileInfo==null || !FileDelFlag.USING.getFlag().equals(fileInfo.getDelFlag())){
				throw new BusinessException(ResponseCodeEnum.CODE_600);
			}
		}

		String[] fileIdArray=fileIds.split(",");

		//查询目标文件中有哪些文件
		FileInfoQuery query=new FileInfoQuery();
		query.setFilePid(filePid);
		query.setUserId(userId);
		List<FileInfo> dbFileList=fileInfoServiceImp.findListByParam(query);

		Map<String,FileInfo> dbFileName=dbFileList.stream().collect(Collectors.toMap(FileInfo::getFileName, Function.identity(),(file1,file2)->file2));

		//查询选中的文件
		query=new FileInfoQuery();
		query.setUserId(userId);
		query.setFileIdArray(fileIdArray);
		List<FileInfo> selectFileList=this.findListByParam(query);

		//将所选文件重命名
		for(FileInfo fileInfo:selectFileList){
			FileInfo rootFileInfo=dbFileName.get(fileInfo.getFileName());
			//如果文件名已经存在 则重命名
			FileInfo updateInfo=new FileInfo();
			if(rootFileInfo!=null){
				String fileName=StringUtils.rename(fileInfo.getFileName());
				updateInfo.setFileName(fileName);
			}
			updateInfo.setFilePid(filePid);
			this.fileInfoMapper.updateByFileIdAndUserId(updateInfo,fileInfo.getFileId(),userId);
		}

	}

	@Override
	public void removeFile2RecycleBatch(String userId, String fileIds) {
		String[] fileIdArray=fileIds.split(",");
		FileInfoQuery query=new FileInfoQuery();
		query.setUserId(userId);
		query.setFileIdArray(fileIdArray);
		query.setDelFlag(FileDelFlag.USING.getFlag());
		List<FileInfo> fileInfoList=this.fileInfoMapper.selectList(query);
		if(fileInfoList.isEmpty()){
			return;
		}
		List<String> delFilePidList=new ArrayList<>();
		for(FileInfo fileInfo:fileInfoList){
			findAllSubFolderList(delFilePidList,userId,fileInfo.getFileId(),FileDelFlag.USING.getFlag());
		}
		//逻辑是这样的，如果选中的是一个文件夹，那么文件夹标记为recycle里面的文件标记为del。如果就是文件，标记为recycle
		if(!delFilePidList.isEmpty()){
			FileInfo updateInfo=new FileInfo();
			updateInfo.setDelFlag(FileDelFlag.DEL.getFlag());
			this.fileInfoMapper.updateFileDelFlagBatch(updateInfo,userId,delFilePidList,null,FileDelFlag.USING.getFlag());
		}
		List<String> delFileIdList= Arrays.asList(fileIdArray);
		FileInfo fileInfo=new FileInfo();
		fileInfo.setRecoveryTime(new Date());
		fileInfo.setDelFlag(FileDelFlag.RECYCLE.getFlag());
		this.fileInfoMapper.updateFileDelFlagBatch(fileInfo,userId,null,delFileIdList,FileDelFlag.USING.getFlag());
	}
	private void findAllSubFolderList(List<String> fileIdList,String userId,String fileId,Integer delFlag){
		fileIdList.add(fileId);
		FileInfoQuery fileInfoQuery=new FileInfoQuery();
		fileInfoQuery.setUserId(userId);
		fileInfoQuery.setFilePid(fileId);
		fileInfoQuery.setDelFlag(delFlag);
		fileInfoQuery.setFolderType(FileFolderTypeEnums.FOLDER.getType());
		List<FileInfo> fileInfoList=this.fileInfoMapper.selectList(fileInfoQuery);
		for(FileInfo fileInfo:fileInfoList){
			findAllSubFolderList(fileIdList,userId,fileInfo.getFilePath(),delFlag);
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void recoverFileBatch(String userId, String fileIds) {
		String[] fileIdArray=fileIds.split(",");
		FileInfoQuery query=new FileInfoQuery();
		query.setUserId(userId);
		query.setFileIdArray(fileIdArray);
		query.setDelFlag(FileDelFlag.RECYCLE.getFlag());
		List<FileInfo> fileInfoList=this.fileInfoMapper.selectList(query);
		List<String> delFileSubFolderFileIdList=new ArrayList<>();
		for(FileInfo fileInfo:fileInfoList){
			if(FileFolderTypeEnums.FOLDER.getType().equals(fileInfo.getFolderType())){
				findAllSubFolderList(delFileSubFolderFileIdList,userId,fileInfo.getFileId(),FileDelFlag.DEL.getFlag());
			}
		}
		//查询所有根目录文件
		query=new FileInfoQuery();
		query.setDelFlag(FileDelFlag.USING.getFlag());
		query.setFilePid(Constants.zero_str);
		List<FileInfo> allRootFileList=this.findListByParam(query);
		Map<String,FileInfo> rootFileMap=allRootFileList.stream().collect(Collectors.toMap(FileInfo::getFileName,Function.identity(),(data1,data2)->data2));
		//查询所有所选文件 将目录下的所有删除文件更新为使用中
		if(!delFileSubFolderFileIdList.isEmpty()){
			FileInfo fileInfo=new FileInfo();
			fileInfo.setDelFlag(FileDelFlag.USING.getFlag());
			this.fileInfoMapper.updateFileDelFlagBatch(fileInfo,userId,delFileSubFolderFileIdList,null,FileDelFlag.DEL.getFlag());
		}

		//将选中的文件更新为正常 且父级目录到根目录
		List<String> delFileIdList=Arrays.asList(fileIdArray);
		FileInfo fileInfo=new FileInfo();
		fileInfo.setDelFlag(FileDelFlag.USING.getFlag());
		fileInfo.setFilePid(Constants.zero_str);
		fileInfo.setLastUpdateTime(new Date());
		this.fileInfoMapper.updateFileDelFlagBatch(fileInfo,userId,null,delFileIdList,FileDelFlag.RECYCLE.getFlag());

		for(FileInfo item:fileInfoList){
			FileInfo rootFileInfo=rootFileMap.get(item.getFileName());
			if(rootFileInfo!=null){
				//如果有同名文件,则需要重命名
				String fileName=StringUtils.rename(item.getFileName());
				FileInfo updateInfo=new FileInfo();
				updateInfo.setFileName(fileName);
				this.fileInfoMapper.updateByFileIdAndUserId(updateInfo, item.getFileId(), userId);
			}
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void delFileBatch(String userId, String fileIds, Boolean adminOp) {
		String[] fileIdArray=fileIds.split(",");
		FileInfoQuery query=new FileInfoQuery();
		query.setUserId(userId);
		query.setFileIdArray(fileIdArray);
		query.setDelFlag(FileDelFlag.RECYCLE.getFlag());
		List<FileInfo> fileInfoList=this.fileInfoMapper.selectList(query);

		List<String> delFileSubFileFolderFileIdList=new ArrayList<>();
		//找到所选文件子目录文件id
		for(FileInfo fileInfo:fileInfoList){
			if(FileFolderTypeEnums.FOLDER.getType().equals(fileInfo.getFolderType())){
				findAllSubFolderList(delFileSubFileFolderFileIdList,userId,fileInfo.getFileId(),FileDelFlag.DEL.getFlag());
			}
		}
		//删除所选文件，子目录中的文件
		if(!delFileSubFileFolderFileIdList.isEmpty()){
			this.fileInfoMapper.delFileBatch(userId,delFileSubFileFolderFileIdList,null,adminOp?null:FileDelFlag.DEL.getFlag());
		}
		//删除所选文件 在转移到回收站的时候文件flag是被设置为recycle的
		this.fileInfoMapper.delFileBatch(userId,null,Arrays.asList(fileIdArray),adminOp?null:FileDelFlag.RECYCLE.getFlag());

		Long useSpace=this.fileInfoMapper.selectUseSpace(userId);
		UserInfo userInfo=new UserInfo();
		userInfo.setUseSpace(useSpace);
		this.userInfoMapper.updateByUserId(userInfo,userId);

		//设置缓存
		UserSpaceDto userSpaceDto= redisComponent.getUserSpaceUse(userId);
		userSpaceDto.setUseSpace(useSpace);
		redisComponent.saveUserSpaceUse(userId,userSpaceDto);
	}

	@Override
	public void checkRootFilePid(String rootFilePid, String userId,String fileId) throws BusinessException {
		if(StringUtils.isEmpty(fileId)){
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		if(rootFilePid.equals(fileId)){
			return;
		}
		checkFilePid(rootFilePid,fileId,userId);
	}
	private void checkFilePid(String rootFilePid,String fileId,String userId) throws BusinessException {
		FileInfo fileInfo=fileInfoMapper.selectByFileIdAndUserId(fileId,userId);
		if(fileInfo==null){
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		if(Constants.zero_str.equals(fileInfo.getFilePid())){
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		if(fileInfo.getFilePid().equals(rootFilePid)){
			return;
		}
		checkFilePid(rootFilePid,fileInfo.getFilePid(),userId);
	}

	@Override
	public void saveShare(String shareRootFilePid, String shareFileIds, String myFolderId, String shareUserId, String curUserId) {
		String[] shareFileIdArray=shareFileIds.split(",");
		//目标文件列表
		FileInfoQuery fileInfoQuery=new FileInfoQuery();
		fileInfoQuery.setUserId(curUserId);
		fileInfoQuery.setFilePid(myFolderId);
		List<FileInfo> currentFileList =this.fileInfoMapper.selectList(fileInfoQuery);
		Map<String,FileInfo> curFileMap=currentFileList.stream().collect(Collectors.toMap(FileInfo::getFileName,Function.identity(),(data1,data2)->data2));

		//选择的文件
		fileInfoQuery =new FileInfoQuery();
		fileInfoQuery.setUserId(shareUserId);
		fileInfoQuery.setFileIdArray(shareFileIdArray);
		List<FileInfo> shareFileList=this.fileInfoMapper.selectList(fileInfoQuery);

		//重命名选择的文件
		List<FileInfo> copyFileList =new ArrayList<>();
		Date curDate=new Date();
		for(FileInfo item:shareFileList){
			FileInfo havaFile=curFileMap.get(item.getFileName());
			if(havaFile!=null) {
				item.setFileId(StringUtils.rename(item.getFileName()));
			}
			findAllSubFile(copyFileList,item,shareUserId,curUserId,curDate,myFolderId);
		}
		this.fileInfoMapper.insertBatch(copyFileList);

	}
	private void findAllSubFile(List<FileInfo> copyFileList,FileInfo fileInfo,String sourceUserId,String currentUserId,Date curDate,String newFilePid){
		String sourceFileId=fileInfo.getFileId();
		fileInfo.setCreateTime(curDate);
		fileInfo.setLastUpdateTime(curDate);
		fileInfo.setFilePid(newFilePid);
		fileInfo.setUserId(currentUserId);
		String newFileId=StringUtils.getRandomString(Constants.length_10);
		fileInfo.setFileId(newFileId);
		copyFileList.add(fileInfo);
		if(FileFolderTypeEnums.FOLDER.getType().equals(fileInfo.getFolderType())){
			FileInfoQuery fileInfoQuery=new FileInfoQuery();
			fileInfoQuery.setFilePid(sourceFileId);
			fileInfoQuery.setUserId(sourceUserId);
			List<FileInfo> sourceFileList=this.fileInfoMapper.selectList(fileInfoQuery);
			for(FileInfo item:sourceFileList){
				findAllSubFile(copyFileList,item,sourceUserId,currentUserId,curDate,newFileId);
			}
		}
	}
}




















