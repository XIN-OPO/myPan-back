package com.mypan.mappers;
import org.apache.ibatis.annotations.Param;

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

}