package com.mypan.mappers;
import com.mypan.entity.po.FileInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FileInfoMapper<T,P> extends BaseMapper {
/**
 * @Description: 文件信息表
 * @Author: 张鑫
 * @Date: 2024/10/06
*/

/**
 *根据FileIdAndUserId查询
*/
	T selectByFileIdAndUserId(@Param("fileId") String fileId, @Param("userId") String userId);

/**
 *根据FileIdAndUserId更新
*/
	Integer updateByFileIdAndUserId(@Param("bean") T t , @Param("fileId") String fileId, @Param("userId") String userId);

/**
 *根据FileIdAndUserId删除
*/
	Integer deleteByFileIdAndUserId(@Param("fileId") String fileId, @Param("userId") String userId);

	Long selectUseSpace(@Param("userId") String userId);

	void updateFileStatusWithOldStatus(@Param("fileId") String fileId,@Param("userId") String userId,@Param("bean") T t,@Param("oldStatus") Integer oldStatus);

	void updateFileDelFlagBatch(@Param("bean") FileInfo fileInfo,
								@Param("userId") String userId,
								@Param("filePidList") List<String> filePidList,
								@Param("fileIdList") List<String> fileIdList,
								@Param("oldDelFlag") Integer oldDelFlag);

	void delFileBatch(@Param("userId") String userId,@Param("filePidList") List<String> filePidList,
					  @Param("fileIdList") List<String> fileIdList,
					  @Param("oldDelFlag") Integer oldDelFlag);

	void delFileByUserId(@Param("userId") String userId);
}
