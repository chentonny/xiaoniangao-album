package com.example.xiaoniangao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.xiaoniangao.entity.MediaTag;

import java.util.List;
import java.util.Map;

public interface MediaTagService extends IService<MediaTag> {

    // 保存媒体文件和标签的关联关系
    boolean saveMediaTags(Long mediaId, String tagsStr);
    
    // 根据媒体ID获取标签列表（包含标签名称）
    List<Map<String, Object>> getTagsByMediaId(Long mediaId);
    
    // 根据媒体ID删除所有关联的标签
    boolean deleteTagsByMediaId(Long mediaId);
    
    // 获取媒体文件的标签名称列表
    List<String> getTagNamesByMediaId(Long mediaId);
}