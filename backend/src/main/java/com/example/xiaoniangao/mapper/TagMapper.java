package com.example.xiaoniangao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.xiaoniangao.entity.Tag;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TagMapper extends BaseMapper<Tag> {

    Tag findByTagName(String tagName);
}