package com.example.xiaoniangao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("media")
public class MediaFile implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    @TableField("user_id")
    private Long userId;
    
    @TableField("title")
    private String title;
    
    @TableField("description")
    private String description;
    
    @TableField("file_path")
    private String filePath;
    
    @TableField("file_type")
    private String fileType;
    
    @TableField("file_size")
    private Long fileSize;
    
    @TableField("cover_path")
    private String coverPath;
    
    @TableField("view_count")
    private Integer viewCount;
    
    @TableField("status")
    private Integer status;
    
    @TableField(value = "create_time", fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    private Date createTime;
    
    @TableField(value = "update_time", fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT_UPDATE)
    private Date updateTime;
    
    // 上传者名称，不存在于数据库中，需要从users表查询
    @TableField(exist = false)
    private String uploaderName;
    
    // 文件标签，不存在于数据库中
    @TableField(exist = false)
    private String tags;
    
    // 原始文件名，不存在于数据库中
    @TableField(exist = false)
    private String originalFileName;
}