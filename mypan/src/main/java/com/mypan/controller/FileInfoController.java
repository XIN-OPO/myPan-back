package com.mypan.controller;

import com.mypan.annotation.GlobalInterceptor;
import com.mypan.annotation.VerifyParam;
import com.mypan.entity.dto.SessionWebUserDto;
import com.mypan.entity.dto.UploadResultDto;
import com.mypan.entity.po.FileInfo;
import com.mypan.entity.query.FileInfoQuery;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
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

import java.util.Arrays;
import java.util.List;

/**
 * @Description: 文件信息表Controller
 * @Author: 张鑫
 * @Date: 2024/10/06
*/
@RestController("fileInfoController")
@RequestMapping("/file")
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
	public void getImage(HttpServletResponse response, @PathVariable("imageFolder") String imageFolder,@PathVariable("imageName") String imageName){
		super.getImage(response,imageFolder,imageName);
	}

	@RequestMapping("/ts/getVideoInfo/{fileId}")
	@GlobalInterceptor(checkParams = true)
	public void getVideo(HttpServletResponse response, HttpSession session,@PathVariable("fileId") String fileId){
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
			//fileInfoQuery.setExcludeFileIdArray(currentFileIds.split(","));
			String[] excludeIds = currentFileIds.split(",");

			/*
			// 加入当前文件夹自身的 ID，确保它不会被排除
			excludeIds = Arrays.copyOf(excludeIds, excludeIds.length + 1);
			excludeIds[excludeIds.length - 1] = filePid; // 添加当前文件夹 ID
			*/

			fileInfoQuery.setExcludeFileIdArray(excludeIds);

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

	@RequestMapping("/createDownloadUrl/{fileId}")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO createDownloadUrl(HttpSession session,
									   @VerifyParam(required = true) @PathVariable("fileId") String fileId

	) throws BusinessException {
		SessionWebUserDto webUserDto=getUserInfoFromSession(session);
		return super.createDownloadUrl(fileId,webUserDto.getUserId());
	}

	@RequestMapping("/download/{code}")
	@GlobalInterceptor(checkParams = true,checkLogin = false)
	public void download(HttpServletResponse response, HttpServletRequest request,
							   @VerifyParam(required = true) @PathVariable("code") String code

	) throws Exception {
		super.download(request,response,code);
	}

	@RequestMapping("/delFile")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO delFile(HttpSession session,
						 @VerifyParam(required = true) String fileIds

	) throws Exception {
		SessionWebUserDto webUserDto=getUserInfoFromSession(session);
		fileInfoService.removeFile2RecycleBatch(webUserDto.getUserId(), fileIds);
		return getSuccessResponseVO(null);
	}
}
