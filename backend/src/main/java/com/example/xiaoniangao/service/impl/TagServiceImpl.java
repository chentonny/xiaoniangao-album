package com.example.xiaoniangao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.xiaoniangao.entity.Tag;
import com.example.xiaoniangao.mapper.TagMapper;
import com.example.xiaoniangao.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService, ApplicationRunner {

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Override
    public void run(ApplicationArguments args) {
        // 应用启动时自动检查并创建tag表
        initTagTable();
    }

    private void initTagTable() {
        try {
            // 检查tag表是否存在
            List<String> tables = jdbcTemplate.queryForList(
                    "SHOW TABLES LIKE 'tag'", String.class);
            
            if (tables.isEmpty()) {
                // 创建tag表
                String createTableSql = "CREATE TABLE tag (" +
                        "id bigint primary key auto_increment comment '自增ID'," +
                        "tag_name varchar(50) not null unique comment '标签名称'," +
                        "count int default 0 comment '标签使用次数'," +
                        "create_time timestamp default current_timestamp comment '创建时间'," +
                        "update_time timestamp default current_timestamp on update current_timestamp comment '更新时间'" +
                        ") comment '标签表';";
                jdbcTemplate.execute(createTableSql);
                
                // 添加索引
                jdbcTemplate.execute("ALTER TABLE tag ADD INDEX idx_tag_name(tag_name);");
                
                // 添加一些初始标签
                addTag("风景");
                addTag("人物");
                addTag("宠物");
                addTag("美食");
                addTag("旅行");
            }
        } catch (Exception e) {
            // 标签表初始化失败，已移除调试输出
        }
    }

    @Override
    public boolean addTag(String tagName) {
        // 检查标签是否已存在
        Tag tag = tagMapper.findByTagName(tagName);
        if (tag == null) {
            tag = new Tag();
            tag.setTagName(tagName);
            tag.setCount(0);
            return this.save(tag);
        }
        return false;
    }

    @Override
    public boolean deleteTag(Long tagId) {
        return this.removeById(tagId);
    }

    @Override
    public Tag findByTagName(String tagName) {
        return tagMapper.findByTagName(tagName);
    }
}