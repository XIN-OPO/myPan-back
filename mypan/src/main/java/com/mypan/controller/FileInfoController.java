package com.mypan.controller;

import com.mypan.entity.po.FileInfo;
import com.mypan.entity.query.FileInfoQuery;
import javax.annotation.Resource;
import com.mypan.service.FileInfoService;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.mypan.entity.vo.ResponseVO;
import java.util.List;

/**
 * @Description: 文件信息表Controller
 * @Author: 张鑫
 * @Date: 2024/10/06
*/
@RestController
@RequestMapping("fileInfo")
public class FileInfoController extends ABaseController {

	@Resource
	private FileInfoService fileInfoService;

	@RequestMapping("loadDataList")
	public ResponseVO loadDataList(FileInfoQuery query) {
		return getSuccessResponseVO(fileInfoService.findListByPage(query));
	}
/**
 *新增
*/

	@RequestMapping("add")
	public ResponseVO add(FileInfo bean) {
		this.fileInfoService.add(bean);
		return getSuccessResponseVO(null);
	}
/**
 *批量新增
*/

	@RequestMapping("addBatch")
	public ResponseVO addBatch(@RequestBody List<FileInfo> listBean) {
		this.fileInfoService.addBatch(listBean);
		return getSuccessResponseVO(null);
	}
/**
 *批量新增或修改
*/

	@RequestMapping("addOrUpdateBatch")
	public ResponseVO addOrUpdateBatch(@RequestBody List<FileInfo> listBean) {
		this.fileInfoService.addOrUpdateBatch(listBean);
		return getSuccessResponseVO(null);
	}

/**
 *根据FileIdAndUserId查询
*/

	@RequestMapping("getFileInfoByFileIdAndUserId")
	public ResponseVO getFileInfoByFileIdAndUserId(String fileId, String userId) {
		return getSuccessResponseVO(this.fileInfoService.getFileInfoByFileIdAndUserId(fileId, userId));
	}
/**
 *根据FileIdAndUserId更新
*/

	@RequestMapping("updateFileInfoByFileIdAndUserId")
	public ResponseVO updateFileInfoByFileIdAndUserId( FileInfo bean , String fileId, String userId) {
		this.fileInfoService.updateFileInfoByFileIdAndUserId(bean,fileId, userId);
		return getSuccessResponseVO(null);
	}

/**
 *根据FileIdAndUserId删除
*/
	@RequestMapping("deleteFileInfoByFileIdAndUserId")
	public ResponseVO deleteFileInfoByFileIdAndUserId(String fileId, String userId) {
		this.fileInfoService.deleteFileInfoByFileIdAndUserId(fileId, userId);
		return getSuccessResponseVO(null);
	}
}
