package com.example.xiaoniangao.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("users")
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @TableField(value = "user_name")
    private String userName;
    private String nickname;
    private String password;
    private String email;
    private String phone;
    private String role = "3"; // 字符串类型，与数据库实际存储类型匹配
    private Integer status = 1; // 0:无效, 1:有效，默认值为1
    
    // 数据库表中存在的创建时间字段，需要自动填充
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    
    // 更新时间字段，如果数据库中有也需要添加
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}