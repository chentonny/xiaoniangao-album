package com.example.xiaoniangao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.xiaoniangao.entity.MediaFile;
import com.example.xiaoniangao.entity.Tag;
import com.example.xiaoniangao.mapper.MediaFileMapper;
import com.example.xiaoniangao.mapper.TagMapper;
import com.example.xiaoniangao.service.MediaFileService;
import com.example.xiaoniangao.service.MediaTagService;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class MediaFileServiceImpl extends ServiceImpl<MediaFileMapper, MediaFile> implements MediaFileService {
    
    private static final Logger log = LoggerFactory.getLogger(MediaFileServiceImpl.class);

    @Autowired
    private MediaFileMapper mediaFileMapper;

    @Autowired
    private TagMapper tagMapper;
    
    @Autowired
    private MediaTagService mediaTagService;

    @Value("${file.upload.path}")
    private String uploadPath;

    @Override
    @Transactional
    public Map<String, Object> uploadFile(MultipartFile file, String fileTitle, String fileDescription, String fileTags, Long uploaderId, String uploaderName) {
        // 兼容旧接口的调用，将参数转发给新逻辑
        return uploadFileInternal(file, fileTitle, fileDescription, fileTags, uploaderId);
    }
    
    /**
     * 内部上传逻辑方法
     */
    @Transactional
    private Map<String, Object> uploadFileInternal(MultipartFile file, String title, String description, String tags, Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 检查文件是否为空
            if (file == null || file.isEmpty()) {
                log.error("上传文件为空");
                throw new RuntimeException("上传文件不能为空");
            }
            
            // 确保上传目录存在
            // 首先尝试使用固定路径作为备选，确保文件能保存成功
            String effectiveUploadPath = "c:\\dev\\media-files\\";
            if (uploadPath != null && !uploadPath.isEmpty()) {
                effectiveUploadPath = uploadPath;
            }
            
            File directory = new File(effectiveUploadPath);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = FilenameUtils.getExtension(originalFilename);
            String newFilename = UUID.randomUUID().toString() + "." + extension;
            String filePath = effectiveUploadPath + newFilename;
            
            // 保存文件
            file.transferTo(new File(filePath));

            // 保存文件信息到数据库
            MediaFile mediaFile = new MediaFile();
            mediaFile.setOriginalFileName(originalFilename);
            mediaFile.setFilePath(filePath);
            
            // 获取文件类型并添加调试日志
            String fileContentType = file.getContentType();
            log.info("文件类型: {}", fileContentType);
            
            // 如果文件类型为null，设置默认值
            if (fileContentType == null) {
                log.warn("文件类型为null，使用默认值 'text/plain'");
                fileContentType = "text/plain";
            }
            
            mediaFile.setFileType(fileContentType);
            mediaFile.setTitle(title);
            mediaFile.setDescription(description);
            mediaFile.setUserId(userId);
            mediaFile.setFileSize(file.getSize());
            mediaFile.setViewCount(0); // 默认0次浏览
            mediaFile.setStatus(1); // 默认状态为可用

            this.save(mediaFile);

            // 使用MediaTagService处理标签
            if (tags != null && !tags.isEmpty()) {
                boolean tagSaved = mediaTagService.saveMediaTags(mediaFile.getId(), tags);
                if (!tagSaved) {
                    throw new RuntimeException("标签保存失败");
                }
            }

            result.put("fileId", mediaFile.getId());
            result.put("filePath", filePath);
            result.put("fileType", file.getContentType());
            result.put("createTime", mediaFile.getCreateTime());
            result.put("userId", userId);
            return result;
        } catch (IOException e) {
            // 文件保存异常
            log.error("文件保存失败: " + e.getMessage(), e);
            throw new RuntimeException("文件保存失败: " + e.getMessage());
        } catch (Exception e) {
            // 其他异常
            log.error("上传处理失败: " + e.getMessage(), e);
            throw new RuntimeException("上传处理失败: " + e.getMessage());
        }
    }

    @Override
    public List<MediaFile> getMyMedia(Long userId, String keyword, int page, int size) {
        int start = (page - 1) * size;
        return mediaFileMapper.findByUserId(userId, keyword, start, size);
    }

    @Override
    public List<MediaFile> getPublicMedia(String keyword, int page, int size) {
        int start = (page - 1) * size;
        return mediaFileMapper.findPublicMedia(keyword, start, size);
    }

    @Override
    public int getMyMediaCount(Long userId, String keyword) {
        return mediaFileMapper.countByUserId(userId, keyword);
    }

    @Override
    public int getPublicMediaCount(String keyword) {
        return mediaFileMapper.countPublicMedia(keyword);
    }

    @Override
    public boolean deleteMedia(Long fileId, Long userId) {
        MediaFile mediaFile = this.getById(fileId);
        if (mediaFile != null && mediaFile.getUserId().equals(userId)) {
            // 删除文件
            File file = new File(mediaFile.getFilePath());
            if (file.exists()) {
                file.delete();
            }
            // 删除数据库记录
            return this.removeById(fileId);
        }
        return false;
    }

    @Override
    public MediaFile getMediaDetail(Long fileId) {
        MediaFile mediaFile = this.getById(fileId);
        if (mediaFile != null) {
            // 获取媒体文件的标签名称列表
            List<String> tagNames = mediaTagService.getTagNamesByMediaId(fileId);
            if (!tagNames.isEmpty()) {
                // 将标签名称列表转换为逗号分隔的字符串
                String tags = String.join(", ", tagNames);
                mediaFile.setTags(tags);
            }
        }
        return mediaFile;
    }

    @Override
    public boolean batchDeleteMedia(List<Long> fileIds) {
        for (Long fileId : fileIds) {
            MediaFile mediaFile = this.getById(fileId);
            if (mediaFile != null) {
                // 删除文件
                File file = new File(mediaFile.getFilePath());
                if (file.exists()) {
                    file.delete();
                }
            }
        }
        // 批量删除数据库记录
        return this.removeByIds(fileIds);
    }
    
    @Override
    public List<MediaFile> getRecentMedia(int page, int limit) {
        // 计算偏移量
        int offset = (page - 1) * limit;
        return mediaFileMapper.findRecentMediaWithUser(offset, limit);
    }
    
    @Override
    public int getRecentMediaCount() {
        return mediaFileMapper.countRecentMedia();
    }
}