package com.mypan.service;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.mypan.entity.dto.SessionShareDto;
import com.mypan.exception.BusinessException;
import org.springframework.format.annotation.DateTimeFormat;
import com.mypan.enums.DateTimePatternEnum;
import com.mypan.utils.DateUtils;

import com.mypan.entity.vo.PaginationResultVO;
import com.mypan.entity.po.FileShare;
import com.mypan.entity.query.FileShareQuery;
import java.util.List;

/**
 * @Description: 分享信息Service
 * @Author: 张鑫
 * @Date: 2024/10/10
*/
public interface FileShareService{

/**
 *根据条件查询列表
*/
	List<FileShare> findListByParam(FileShareQuery query);

/**
 *根据条件查询数量
*/
	Integer findCountByParam(FileShareQuery query);

/**
 *分页查询
*/
	PaginationResultVO<FileShare> findListByPage(FileShareQuery query);

/**
 *新增
*/
	Integer add(FileShare bean);

/**
 *批量新增
*/
	Integer addBatch(List<FileShare> listBean);

/**
 *批量新增或修改
*/
	Integer addOrUpdateBatch(List<FileShare> listBean);


/**
 *根据ShareId查询
*/
	FileShare getFileShareByShareId(String shareId);

/**
 *根据ShareId更新
*/
	Integer updateFileShareByShareId( FileShare bean , String shareId);

/**
 *根据ShareId删除
*/
	Integer deleteFileShareByShareId(String shareId);

	void saveShare(FileShare share) throws BusinessException;

	void delFileShareBatch(String[] shareIdArray,String userId) throws BusinessException;

	SessionShareDto checkShareCode(String shareId,String code) throws BusinessException;
}
