package com.example.xiaoniangao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.xiaoniangao.entity.MediaFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface MediaFileService extends IService<MediaFile> {

    Map<String, Object> uploadFile(MultipartFile file, String fileTitle, String fileDescription, String fileTag, Long uploaderId, String uploaderName);

    List<MediaFile> getMyMedia(Long userId, String keyword, int page, int size);

    List<MediaFile> getPublicMedia(String keyword, int page, int size);

    int getMyMediaCount(Long userId, String keyword);

    int getPublicMediaCount(String keyword);

    boolean deleteMedia(Long fileId, Long userId);

    MediaFile getMediaDetail(Long fileId);

    boolean batchDeleteMedia(List<Long> fileIds);
    
    // 新增方法：获取最新媒体数据
    List<MediaFile> getRecentMedia(int page, int limit);
    
    // 获取最新媒体总数
    int getRecentMediaCount();
}