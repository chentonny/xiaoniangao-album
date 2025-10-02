package com.example.xiaoniangao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.xiaoniangao.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    // findByUserName方法的SQL在XML文件中定义
    User findByUserName(String userName);
}