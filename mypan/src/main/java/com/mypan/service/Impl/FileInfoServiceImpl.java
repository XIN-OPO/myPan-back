package com.mypan.service.Impl;
import com.mypan.entity.vo.PaginationResultVO;
import com.mypan.entity.query.SimplePage;
import com.mypan.enums.PageSize;
import com.mypan.entity.po.FileInfo;
import com.mypan.entity.query.FileInfoQuery;
import javax.annotation.Resource;
import com.mypan.mappers.FileInfoMapper;
import com.mypan.service.FileInfoService;
import org.springframework.stereotype.Service;
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
}
