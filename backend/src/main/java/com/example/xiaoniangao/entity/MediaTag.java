package com.example.xiaoniangao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("media_tag")
public class MediaTag implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    @TableField("media_id")
    private Long mediaId;
    
    @TableField("tag_id")
    private Long tagId;
    
    @TableField("create_time")
    private Date createTime;
}