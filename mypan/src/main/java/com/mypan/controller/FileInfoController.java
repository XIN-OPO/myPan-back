package com.mypan.controller;

import com.mypan.annotation.GlobalInterceptor;
import com.mypan.annotation.VerifyParam;
import com.mypan.entity.dto.SessionWebUserDto;
import com.mypan.entity.dto.UploadResultDto;
import com.mypan.entity.po.FileInfo;
import com.mypan.entity.query.FileInfoQuery;
import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import com.mypan.entity.vo.FileInfoVO;
import com.mypan.entity.vo.PaginationResultVO;
import com.mypan.enums.FileCategoryEnums;
import com.mypan.enums.FileDelFlag;
import com.mypan.exception.BusinessException;
import com.mypan.service.FileInfoService;
import org.springframework.boot.web.servlet.server.Session;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
public class FileInfoController extends ABaseController {

	@Resource
	private FileInfoService fileInfoService;


	@RequestMapping("loadDataList")
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

	@RequestMapping("uploadFile")
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
}
