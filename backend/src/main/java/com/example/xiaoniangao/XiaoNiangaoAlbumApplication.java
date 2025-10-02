package com.example.xiaoniangao;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.util.unit.DataSize;

import javax.servlet.MultipartConfigElement;

@SpringBootApplication
@MapperScan("com.example.xiaoniangao.mapper")
public class XiaoNiangaoAlbumApplication {

    public static void main(String[] args) {
        SpringApplication.run(XiaoNiangaoAlbumApplication.class, args);
    }

    // 配置文件上传大小限制
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        // 单个文件最大
        factory.setMaxFileSize(DataSize.ofMegabytes(200));
        // 设置总上传数据总大小
        factory.setMaxRequestSize(DataSize.ofMegabytes(200));
        return factory.createMultipartConfig();
    }
}