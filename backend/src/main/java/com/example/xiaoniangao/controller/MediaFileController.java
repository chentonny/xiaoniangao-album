package com.example.xiaoniangao.controller;

import com.example.xiaoniangao.entity.MediaFile;
import com.example.xiaoniangao.service.MediaFileService;
import com.example.xiaoniangao.config.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * 媒体文件控制器
 */
@RestController
@RequestMapping("/media")
public class MediaFileController {

    @Autowired
    private MediaFileService mediaFileService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Value("${file.upload.path}")
    private String uploadPath;

    // 文件上传接口 - 增强版
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("tags") String tags,
            HttpServletRequest request) {
        try {
            // 检查文件是否为空
            if (file == null || file.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 0);
                response.put("message", "文件为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 从请求头获取JWT令牌并解析用户ID
            String token = request.getHeader("Authorization");
            Long userId = null;
            String userName = "unknown";
            
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                try {
                    userId = jwtTokenUtil.getUserIdFromToken(token);
                    userName = jwtTokenUtil.getUsernameFromToken(token);
                } catch (Exception e) {
                    // JWT令牌解析异常，不中断流程，继续使用默认值
                }
            }
            
            // 确保用户ID存在
            if (userId == null) {
                // 用户未登录，返回错误
                Map<String, Object> response = new HashMap<>();
                response.put("code", 0);
                response.put("message", "用户未登录，无法上传文件");
                response.put("data", new HashMap<>());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // 调用服务层上传文件
            Map<String, Object> data = mediaFileService.uploadFile(file, title, description, tags, userId, userName);
            
            if (data != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 1);
                response.put("message", "上传成功");
                response.put("data", data);
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 0);
                response.put("message", "上传失败：文件处理异常");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 0);
            response.put("message", "上传失败: " + e.getMessage());
            response.put("errorType", e.getClass().getName());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("my-media")
    public Map<String, Object> getMyMedia(
            @RequestParam("userId") Long userId,
            @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "12") int size) {

        Map<String, Object> result = new HashMap<>();
        List<MediaFile> mediaList = mediaFileService.getMyMedia(userId, keyword, page, size);
        int total = mediaFileService.getMyMediaCount(userId, keyword);
        
        result.put("code", 1);
        result.put("message", "success");
        result.put("data", mediaList);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        
        return result;
    }

    @GetMapping("public-media")
    public Map<String, Object> getPublicMedia(
            @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "20") int size) {

        Map<String, Object> result = new HashMap<>();
        // 调用支持关键词搜索的getPublicMedia和getPublicMediaCount方法
        List<MediaFile> mediaList = mediaFileService.getPublicMedia(keyword, page, size);
        int total = mediaFileService.getPublicMediaCount(keyword);
        
        result.put("code", 1);
        result.put("message", "success");
        result.put("data", mediaList);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        
        return result;
    }

    @GetMapping("media-detail")
    public Map<String, Object> getMediaDetail(@RequestParam("fileId") Long fileId) {
        Map<String, Object> result = new HashMap<>();
        MediaFile mediaFile = mediaFileService.getMediaDetail(fileId);
        
        if (mediaFile != null) {
            result.put("code", 1);
            result.put("message", "success");
            result.put("data", mediaFile);
        } else {
            result.put("code", 0);
            result.put("message", "文件不存在");
            result.put("data", new HashMap<>());
        }
        return result;
    }

    @DeleteMapping("delete-media")
    public Map<String, Object> deleteMedia(@RequestParam("fileId") Long fileId, @RequestParam("userId") Long userId) {
        Map<String, Object> result = new HashMap<>();
        boolean success = mediaFileService.deleteMedia(fileId, userId);
        
        if (success) {
            result.put("code", 1);
            result.put("message", "success");
        } else {
            result.put("code", 0);
            result.put("message", "删除失败");
        }
        result.put("data", new HashMap<>());
        return result;
    }

    @DeleteMapping("batch-delete-media")
    public Map<String, Object> batchDeleteMedia(@RequestBody List<Long> fileIds) {
        Map<String, Object> result = new HashMap<>();
        boolean success = mediaFileService.batchDeleteMedia(fileIds);
        
        if (success) {
            result.put("code", 1);
            result.put("message", "success");
        } else {
            result.put("code", 0);
            result.put("message", "删除失败");
        }
        result.put("data", new HashMap<>());
        return result;
    }

    @GetMapping("download-media")
    public void downloadMedia(@RequestParam("fileId") Long fileId, HttpServletResponse response) {
        MediaFile mediaFile = mediaFileService.getById(fileId);
        if (mediaFile != null) {
            File file = new File(mediaFile.getFilePath());
            if (file.exists()) {
                response.setContentType("application/octet-stream");
                response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(mediaFile.getFilePath()));
                
                try (InputStream in = new FileInputStream(file); OutputStream out = response.getOutputStream()) {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = in.read(buffer)) > 0) {
                        out.write(buffer, 0, len);
                    }
                } catch (IOException e) {
                    // 异常已捕获，移除调试输出
                }
            }
        }
    }
    
    // 新增API：获取最新媒体数据（支持分页）
    @GetMapping(value = "recent", produces = {"application/json;charset=utf-8"})
    public Map<String, Object> getRecentMedia(
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "limit", required = false, defaultValue = "8") int limit) {
        Map<String, Object> result = new HashMap<>();
        List<MediaFile> mediaList = mediaFileService.getRecentMedia(page, limit);
        int total = mediaFileService.getRecentMediaCount();
        
        result.put("code", 1);
        result.put("message", "success");
        result.put("data", mediaList);
        result.put("total", total);
        result.put("page", page);
        result.put("limit", limit);
        return result;
    }
}