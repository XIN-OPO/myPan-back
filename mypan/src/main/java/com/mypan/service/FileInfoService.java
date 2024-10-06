package com.mypan.service;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.mypan.entity.dto.SessionWebUserDto;
import com.mypan.entity.dto.UploadResultDto;
import com.mypan.exception.BusinessException;
import org.springframework.format.annotation.DateTimeFormat;
import com.mypan.enums.DateTimePatternEnum;
import com.mypan.utils.DateUtils;

import com.mypan.entity.vo.PaginationResultVO;
import com.mypan.entity.po.FileInfo;
import com.mypan.entity.query.FileInfoQuery;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @Description: 文件信息表Service
 * @Author: 张鑫
 * @Date: 2024/10/06
*/
public interface FileInfoService{

/**
 *根据条件查询列表
*/
	List<FileInfo> findListByParam(FileInfoQuery query);

/**
 *根据条件查询数量
*/
	Integer findCountByParam(FileInfoQuery query);

/**
 *分页查询
*/
	PaginationResultVO<FileInfo> findListByPage(FileInfoQuery query);

/**
 *新增
*/
	Integer add(FileInfo bean);

/**
 *批量新增
*/
	Integer addBatch(List<FileInfo> listBean);

/**
 *批量新增或修改
*/
	Integer addOrUpdateBatch(List<FileInfo> listBean);


/**
 *根据FileIdAndUserId查询
*/
	FileInfo getFileInfoByFileIdAndUserId(String fileId, String userId);

/**
 *根据FileIdAndUserId更新
*/
	Integer updateFileInfoByFileIdAndUserId( FileInfo bean , String fileId, String userId);

/**
 *根据FileIdAndUserId删除
*/
	Integer deleteFileInfoByFileIdAndUserId(String fileId, String userId);

	UploadResultDto uploadFile(SessionWebUserDto userDto, String fileId, MultipartFile file, String fileName, String filePid, String fileMd5, Integer chunkIndex, Integer chunks) throws BusinessException;
}
