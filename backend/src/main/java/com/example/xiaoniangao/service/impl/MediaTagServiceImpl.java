package com.example.xiaoniangao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.xiaoniangao.entity.MediaTag;
import com.example.xiaoniangao.entity.Tag;
import com.example.xiaoniangao.mapper.MediaTagMapper;
import com.example.xiaoniangao.mapper.TagMapper;
import com.example.xiaoniangao.service.MediaTagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class MediaTagServiceImpl extends ServiceImpl<MediaTagMapper, MediaTag> implements MediaTagService {
    
    private static final Logger log = LoggerFactory.getLogger(MediaTagServiceImpl.class);

    @Autowired
    private MediaTagMapper mediaTagMapper;
    
    @Autowired
    private TagMapper tagMapper;

    @Override
    @Transactional
    public boolean saveMediaTags(Long mediaId, String tagsStr) {
        if (mediaId == null || tagsStr == null || tagsStr.isEmpty()) {
            return false;
        }
        
        // 首先删除该媒体文件的所有现有标签
        mediaTagMapper.deleteByMediaId(mediaId);
        
        // 处理标签字符串
        String[] tagArray = tagsStr.split(",");
        List<MediaTag> mediaTagList = new ArrayList<>();
        
        for (String tagName : tagArray) {
            tagName = tagName.trim();
            if (tagName.isEmpty()) {
                continue;
            }
            
            // 查找或创建标签
            Tag tag = tagMapper.findByTagName(tagName);
            if (tag == null) {
                tag = new Tag();
                tag.setTagName(tagName);
                tag.setCount(1);
                tagMapper.insert(tag);
            } else {
                // 更新标签使用次数
                tag.setCount(tag.getCount() + 1);
                tagMapper.updateById(tag);
            }
            
            // 创建媒体标签关联
            MediaTag mediaTag = new MediaTag();
            mediaTag.setMediaId(mediaId);
            mediaTag.setTagId(tag.getId());
            mediaTag.setCreateTime(new Date());
            mediaTagList.add(mediaTag);
        }
        
        // 批量保存媒体标签关联
        if (!mediaTagList.isEmpty()) {
            return this.saveBatch(mediaTagList);
        }
        
        return true;
    }

    @Override
    public List<Map<String, Object>> getTagsByMediaId(Long mediaId) {
        if (mediaId == null) {
            return new ArrayList<>();
        }
        return mediaTagMapper.findTagsByMediaId(mediaId);
    }

    @Override
    @Transactional
    public boolean deleteTagsByMediaId(Long mediaId) {
        if (mediaId == null) {
            return false;
        }
        
        // 获取该媒体文件的所有标签ID
        List<Long> tagIds = mediaTagMapper.findTagIdsByMediaId(mediaId);
        
        // 删除媒体标签关联
        int deletedCount = mediaTagMapper.deleteByMediaId(mediaId);
        
        // 更新标签使用次数
        for (Long tagId : tagIds) {
            Tag tag = tagMapper.selectById(tagId);
            if (tag != null && tag.getCount() > 0) {
                tag.setCount(tag.getCount() - 1);
                tagMapper.updateById(tag);
            }
        }
        
        return deletedCount > 0;
    }

    @Override
    public List<String> getTagNamesByMediaId(Long mediaId) {
        if (mediaId == null) {
            return new ArrayList<>();
        }
        
        List<Map<String, Object>> tags = mediaTagMapper.findTagsByMediaId(mediaId);
        List<String> tagNames = new ArrayList<>();
        
        for (Map<String, Object> tag : tags) {
            tagNames.add((String) tag.get("tag_name"));
        }
        
        return tagNames;
    }
}