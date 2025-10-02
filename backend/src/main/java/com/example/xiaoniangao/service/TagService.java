package com.example.xiaoniangao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.xiaoniangao.entity.Tag;

public interface TagService extends IService<Tag> {

    boolean addTag(String tagName);

    boolean deleteTag(Long tagId);

    Tag findByTagName(String tagName);
}