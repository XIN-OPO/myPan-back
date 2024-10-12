package com.mypan.mappers;
import org.apache.ibatis.annotations.Param;

public interface FileShareMapper<T,P> extends BaseMapper {
/**
 * @Description: 分享信息
 * @Author: 张鑫
 * @Date: 2024/10/10
*/

/**
 *根据ShareId查询
*/
	T selectByShareId(@Param("shareId") String shareId);

/**
 *根据ShareId更新
*/
	Integer updateByShareId(@Param("bean") T t , @Param("shareId") String shareId);

/**
 *根据ShareId删除
*/
	Integer deleteByShareId(@Param("shareId") String shareId);

	Integer delFileShareBatch(@Param("shareIdArray")String[] shareIdArray,@Param("userId") String userId);

	void updateShareShowCount(@Param("shareId") String shareId);
}
