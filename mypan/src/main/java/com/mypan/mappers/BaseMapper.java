package com.mypan.mappers;

import java.util.List;
import org.apache.ibatis.annotations.Param;
public interface BaseMapper<T,P>{
    /*
    insert:插入
     */
    Integer insert(@Param("bean") T t);
    /*
    插入或者更新
     */
    Integer insertOrUpdate(@Param("bean") T t);
    /*
    批量插入
     */
    Integer insertBatch(@Param("list") List<T> list);
    /*
    批量插入或者更新
     */
    Integer insertOrUpdateBatch(@Param("list") List<T> list);
    /*
    根据参数查询集合
     */
    List<T> selectList(@Param("query") P p);
    /*
    根据集合查询数量
     */
    Integer selectCount(@Param("query") P p);
}
