package com.example.xiaoniangao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.xiaoniangao.entity.MediaTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface MediaTagMapper extends BaseMapper<MediaTag> {

    // 根据媒体ID获取标签ID列表
    List<Long> findTagIdsByMediaId(@Param("mediaId") Long mediaId);
    
    // 根据媒体ID删除所有关联的标签
    int deleteByMediaId(@Param("mediaId") Long mediaId);
    
    // 根据标签ID删除所有关联的媒体标签记录
    int deleteByTagId(@Param("tagId") Long tagId);
    
    // 获取媒体文件的标签信息（包含标签名称）
    List<Map<String, Object>> findTagsByMediaId(@Param("mediaId") Long mediaId);
}