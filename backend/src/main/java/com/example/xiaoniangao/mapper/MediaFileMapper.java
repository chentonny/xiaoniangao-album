package com.example.xiaoniangao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.xiaoniangao.entity.MediaFile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MediaFileMapper extends BaseMapper<MediaFile> {

    List<MediaFile> findByUserId(@Param("userId") Long userId, @Param("keyword") String keyword, @Param("start") int start, @Param("size") int size);

    List<MediaFile> findPublicMedia(@Param("keyword") String keyword, @Param("start") int start, @Param("size") int size);

    int countByUserId(@Param("userId") Long userId, @Param("keyword") String keyword);

    int countPublicMedia(@Param("keyword") String keyword);
    
    // 新增方法：获取最新媒体并关联用户表获取用户名（支持分页）
    List<MediaFile> findRecentMediaWithUser(@Param("offset") int offset, @Param("limit") int limit);
    
    // 获取最新媒体总数
    int countRecentMedia();
}