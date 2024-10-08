package com.mypan.controller;

import com.mypan.annotation.GlobalInterceptor;
import com.mypan.annotation.VerifyParam;
import com.mypan.entity.dto.SessionWebUserDto;
import com.mypan.entity.dto.UploadResultDto;
import com.mypan.entity.po.FileInfo;
import com.mypan.entity.query.FileInfoQuery;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.mypan.entity.vo.FileInfoVO;
import com.mypan.entity.vo.PaginationResultVO;
import com.mypan.enums.FileCategoryEnums;
import com.mypan.enums.FileDelFlag;
import com.mypan.enums.FileFolderTypeEnums;
import com.mypan.exception.BusinessException;
import com.mypan.service.FileInfoService;
import com.mypan.utils.CopyTools;
import com.mypan.utils.StringUtils;
import org.springframework.boot.web.servlet.server.Session;
import org.springframework.web.bind.annotation.*;
import com.mypan.entity.vo.ResponseVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @Description: 文件信息表Controller
 * @Author: 张鑫
 * @Date: 2024/10/06
*/
@RestController("fileInfoController")
@RequestMapping("file")
public class FileInfoController extends CommonFileController {

	@Resource
	private FileInfoService fileInfoService;


	@RequestMapping("/loadDataList")
	@GlobalInterceptor
	public ResponseVO loadDataList(HttpSession session, FileInfoQuery query,String fileCategory) {
		FileCategoryEnums categoryEnums=FileCategoryEnums.getByCode(fileCategory);
		if(null!=categoryEnums){
			query.setFileCategary(categoryEnums.getCategory());
		}
		query.setUserId(getUserInfoFromSession(session).getUserId());
		query.setOrderBy("last_update_time desc");
		query.setDelFlag(FileDelFlag.USING.getFlag());
		PaginationResultVO resultVO=fileInfoService.findListByPage(query);
		return getSuccessResponseVO(convert2PaginationResultVO(resultVO, FileInfoVO.class));
	}

	@RequestMapping("/uploadFile")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO uploadFile(HttpSession session,
								 String fileId,
								 MultipartFile file,
								 @VerifyParam(required = true) String fileName,
								 @VerifyParam(required = true) String filePid,
								 @VerifyParam(required = true) String fileMd5,
								 @VerifyParam(required = true) Integer chunkIndex,
								 @VerifyParam(required = true) Integer chunks) throws BusinessException {
		SessionWebUserDto userDto=getUserInfoFromSession(session);
		UploadResultDto uploadResultDto=fileInfoService.uploadFile(userDto,fileId,file,fileName,filePid,fileMd5,chunkIndex,chunks);
		return getSuccessResponseVO(uploadResultDto);
	}

	@RequestMapping("/getImage/{imageFolder}/{imageName}")
	@GlobalInterceptor(checkParams = true)
	public void getImage(HttpServletResponse response, @PathVariable("imageFolder") String imageFolder,@PathVariable("imageName") String imageName){
		super.getImage(response,imageFolder,imageName);
	}

	@RequestMapping("/ts/getVideoInfo/{fileId}")
	@GlobalInterceptor(checkParams = true)
	public void getImage(HttpServletResponse response, HttpSession session,@PathVariable("fileId") String fileId){
		SessionWebUserDto webUserDto=getUserInfoFromSession(session);
		super.getFile(response,fileId, webUserDto.getUserId());
	}

	@RequestMapping("/getFile/{fileId}")
	@GlobalInterceptor(checkParams = true)
	public void getFile(HttpServletResponse response, HttpSession session,@PathVariable("fileId") String fileId){
		SessionWebUserDto webUserDto=getUserInfoFromSession(session);
		super.getFile(response,fileId, webUserDto.getUserId());
	}

	//创建文件夹
	@RequestMapping("/newFoloder")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO newFoloder(HttpSession session,
						   @VerifyParam(required = true) String filePid,
						   @VerifyParam(required = true) String fileName) throws BusinessException {
		SessionWebUserDto webUserDto=getUserInfoFromSession(session);
		FileInfo fileInfo=fileInfoService.newFolder(filePid, webUserDto.getUserId(), fileName);
		return getSuccessResponseVO(CopyTools.copy(fileInfo,FileInfoVO.class));
	}

	//获取文件夹信息
	@RequestMapping("/getFolderInfo")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO getFolderInfo(HttpSession session,
								 @VerifyParam(required = true) String path) throws BusinessException {
		SessionWebUserDto webUserDto=getUserInfoFromSession(session);
		return super.getFolderInfo(path, webUserDto.getUserId());
	}

	//文件的重命名
	@RequestMapping("/rename")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO rename(HttpSession session,
							 @VerifyParam(required = true) String fileId,
							 String fileName) throws BusinessException {
		SessionWebUserDto webUserDto=getUserInfoFromSession(session);
		FileInfo fileInfo=fileInfoService.rename(fileId,webUserDto.getUserId(),fileName);
		return getSuccessResponseVO(CopyTools.copy(fileInfo,FileInfoVO.class));
	}

	//获取所有目录
	@RequestMapping("/loadAllFolder")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO loadAllFolder(HttpSession session,
							 @VerifyParam(required = true) String filePid,
							 @VerifyParam(required = true) String currentFileIds) throws BusinessException {
		SessionWebUserDto webUserDto=getUserInfoFromSession(session);
		FileInfoQuery fileInfoQuery=new FileInfoQuery();
		fileInfoQuery.setUserId(webUserDto.getUserId());
		fileInfoQuery.setFilePid(filePid);
		fileInfoQuery.setFolderType(FileFolderTypeEnums.FOLDER.getType());
		if(!StringUtils.isEmpty(currentFileIds)){
			String[] fileArray=currentFileIds.split(",");
			fileInfoQuery.setExcludeFileIdArray(fileArray);
		}
		fileInfoQuery.setDelFlag(FileDelFlag.USING.getFlag());
		fileInfoQuery.setOrderBy("create_time desc");
		List<FileInfo> fileInfoList=fileInfoService.findListByParam(fileInfoQuery);
		return getSuccessResponseVO(CopyTools.copyList(fileInfoList,FileInfoVO.class));
	}

	//移动文件
	@RequestMapping("/changeFileFolder")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO changeFileFolder(HttpSession session,
							 @VerifyParam(required = true) String fileIds,
							 @VerifyParam(required = true) String filePid
							 ) throws BusinessException {
		SessionWebUserDto webUserDto=getUserInfoFromSession(session);
		fileInfoService.changeFileFolder(fileIds,filePid, webUserDto.getUserId());
		return getSuccessResponseVO(null);
	}

}
