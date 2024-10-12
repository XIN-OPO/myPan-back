package com.mypan.service.Impl;
import com.mypan.entity.constants.Constants;
import com.mypan.entity.dto.SessionShareDto;
import com.mypan.entity.vo.PaginationResultVO;
import com.mypan.entity.query.SimplePage;
import com.mypan.enums.PageSize;
import com.mypan.entity.po.FileShare;
import com.mypan.entity.query.FileShareQuery;
import javax.annotation.Resource;

import com.mypan.enums.ResponseCodeEnum;
import com.mypan.enums.ShareValidTypeEnum;
import com.mypan.exception.BusinessException;
import com.mypan.mappers.FileShareMapper;
import com.mypan.service.FileShareService;
import com.mypan.utils.DateUtils;
import com.mypan.utils.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * @Description: 分享信息ServiceImpl
 * @Author: 张鑫
 * @Date: 2024/10/10
*/
@Service("fileShareService")
public class FileShareServiceImpl implements FileShareService {

	@Resource
	private FileShareMapper<FileShare,FileShareQuery> fileShareMapper;

/**
 *根据条件查询列表
*/
	public List<FileShare> findListByParam(FileShareQuery query) {
		return this.fileShareMapper.selectList(query);
	}
/**
 *根据条件查询数量
*/
	public Integer findCountByParam(FileShareQuery query) {
		return this.fileShareMapper.selectCount(query);
	}
/**
 *分页查询
*/
	public PaginationResultVO<FileShare> findListByPage(FileShareQuery query) {
		Integer count=this.findCountByParam(query);
		Integer pageSize=query.getPageSize()==null?PageSize.SIZE15.getSize():query.getPageSize();
		SimplePage page=new SimplePage(query.getPageNo(),count,pageSize);
		query.setSimplePage(page);
		List<FileShare> list=this.findListByParam(query);
		PaginationResultVO<FileShare> result=new PaginationResultVO(count,page.getPageSize(),page.getPageNo(),page.getPageTotal(),list);
		return result;
	}
/**
 *新增
*/
	public Integer add(FileShare bean) {
		return this.fileShareMapper.insert(bean);
	}
/**
 *批量新增
*/
	public Integer addBatch(List<FileShare> listBean) {
		if (listBean==null || listBean.isEmpty()) {
			return 0;
		}
		return this.fileShareMapper.insertBatch(listBean);
	}
/**
 *批量新增或修改
*/
	public Integer addOrUpdateBatch(List<FileShare> listBean) {
		if (listBean==null || listBean.isEmpty()) {
			return 0;
		}
		return this.fileShareMapper.insertOrUpdateBatch(listBean);
	}

/**
 *根据ShareId查询
*/
	public FileShare getFileShareByShareId(String shareId) {
		return this.fileShareMapper.selectByShareId(shareId);
	}
/**
 *根据ShareId更新
*/
	public Integer updateFileShareByShareId( FileShare bean , String shareId) {
		return this.fileShareMapper.updateByShareId(bean,shareId);
	}

/**
 *根据ShareId删除
*/
	public Integer deleteFileShareByShareId(String shareId) {
		return this.fileShareMapper.deleteByShareId(shareId);
	}

	@Override
	public void saveShare(FileShare share) throws BusinessException {
		ShareValidTypeEnum typeEnum=ShareValidTypeEnum.getByType(share.getValidType());
		if(typeEnum==null){
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		if(ShareValidTypeEnum.FOREVER!=typeEnum){
			share.setExpireTime(DateUtils.getAfterDate(typeEnum.getDays()));
		}
		Date curDate=new Date();
		share.setShareTime(curDate);
		if(StringUtils.isEmpty(share.getCode())){
			share.setCode(StringUtils.getRandomString(Constants.length_5));
		}
		share.setShareId(StringUtils.getRandomString(Constants.length_20));
		this.fileShareMapper.insert(share);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void delFileShareBatch(String[] shareIdArray, String userId) throws BusinessException {
		Integer count=this.fileShareMapper.delFileShareBatch(shareIdArray,userId);
		if(count!=shareIdArray.length){
			throw  new BusinessException(ResponseCodeEnum.CODE_600);
		}
	}

	@Override
	public SessionShareDto checkShareCode(String shareId, String code) throws BusinessException {
		FileShare share=this.fileShareMapper.selectByShareId(shareId);
		if(null==share || (share.getExpireTime()!=null && new Date().after(share.getExpireTime()))){
			throw new BusinessException(ResponseCodeEnum.CODE_902.getMsg());
		}
		if(!share.getCode().equals(code)){
			throw new BusinessException("提取码错误");
		}
		//更新浏览次数
		this.fileShareMapper.updateShareShowCount(shareId);
		SessionShareDto sessionShareDto=new SessionShareDto();
		sessionShareDto.setShareId(shareId);
		sessionShareDto.setShareUserId(share.getUserId());
		sessionShareDto.setFileId(share.getFileId());
		sessionShareDto.setExpireTime(share.getExpireTime());
		return sessionShareDto;
	}
}
