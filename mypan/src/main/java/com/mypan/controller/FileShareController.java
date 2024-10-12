package com.mypan.controller;

import com.mypan.annotation.GlobalInterceptor;
import com.mypan.annotation.VerifyParam;
import com.mypan.entity.dto.SessionWebUserDto;
import com.mypan.entity.po.FileShare;
import com.mypan.entity.query.FileShareQuery;
import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import com.mypan.entity.vo.PaginationResultVO;
import com.mypan.exception.BusinessException;
import com.mypan.service.FileShareService;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.mypan.entity.vo.ResponseVO;
import java.util.List;

/**
 * @Description: 分享信息Controller
 * @Author: 张鑫
 * @Date: 2024/10/10
*/
@RestController("fileShareController")
@RequestMapping("/share")
public class FileShareController extends ABaseController {

	@Resource
	private FileShareService fileShareService;

	@RequestMapping("/loadShareList")
	@GlobalInterceptor
	public ResponseVO loadShareList(HttpSession session, FileShareQuery query) {
		query.setOrderBy("share_time desc");
		SessionWebUserDto webUserDto=getUserInfoFromSession(session);
		query.setUserId(webUserDto.getUserId());
		query.setQueryFileName(true);
		PaginationResultVO result=fileShareService.findListByPage(query);
		return getSuccessResponseVO(result);
	}

	@RequestMapping("/shareFile")
	@GlobalInterceptor(checkParams = true )
	public ResponseVO shareFile(HttpSession session, @VerifyParam(required = true) String fileId,
								@VerifyParam(required = true) Integer validType,
								String code) throws BusinessException {
		SessionWebUserDto webUserDto=getUserInfoFromSession(session);
		FileShare share=new FileShare();
		share.setValidType(validType);
		share.setCode(code);
		share.setFileId(fileId);
		share.setUserId(webUserDto.getUserId());
		fileShareService.saveShare(share);
		return getSuccessResponseVO(share);
	}

	@RequestMapping("/cancelShare")
	@GlobalInterceptor(checkParams = true )
	public ResponseVO cancelShare(HttpSession session, @VerifyParam(required = true) String shareIds
								) throws BusinessException {
		SessionWebUserDto webUserDto=getUserInfoFromSession(session);
		fileShareService.delFileShareBatch(shareIds.split(","), webUserDto.getUserId());
		return getSuccessResponseVO(null);
	}
}
