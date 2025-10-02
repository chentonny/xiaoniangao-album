package com.example.xiaoniangao.controller;

import com.example.xiaoniangao.entity.MediaFile;
import com.example.xiaoniangao.entity.Tag;
import com.example.xiaoniangao.entity.User;
import com.example.xiaoniangao.service.MediaFileService;
import com.example.xiaoniangao.service.TagService;
import com.example.xiaoniangao.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.io.File;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private TagService tagService;

    @Autowired
    private MediaFileService mediaFileService;

    // 用户管理
    @GetMapping("users")
    public Map<String, Object> getUsers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 确保users不为null
            List<User> users = userService.list() != null ? userService.list() : new ArrayList<>();
            
            // 实现搜索功能
            if (keyword != null && !keyword.isEmpty()) {
                users = users.stream()
                        .filter(user -> user != null && 
                                (user.getUserName() != null && user.getUserName().contains(keyword) || 
                                (user.getNickname() != null && user.getNickname().contains(keyword))))
                        .collect(java.util.stream.Collectors.toList());
            }
            
            // 实现分页功能
            int total = users.size();
            int start = (page - 1) * pageSize;
            int end = Math.min(start + pageSize, total);
            List<User> pageUsers = start < total ? users.subList(start, end) : new ArrayList<>();
            
            // 构建符合前端期望格式的返回数据
            Map<String, Object> data = new HashMap<>();
            data.put("usersList", pageUsers);
            data.put("total", total);
            
            result.put("code", 1);
            result.put("message", "success");
            result.put("data", data);
        } catch (Exception e) {
            // 捕获所有异常并返回友好错误信息
            result.put("code", 500);
            result.put("message", "服务器内部错误: " + e.getMessage());
            result.put("data", new HashMap<>());
        }
        return result;
    }

    @DeleteMapping("delete-user")
    public Map<String, Object> deleteUser(@RequestParam("userId") Long userId) {
        Map<String, Object> result = new HashMap<>();
        boolean success = userService.deleteUser(userId);
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

    // 标签管理
    @GetMapping("tags")
    public Map<String, Object> getTags() {
        Map<String, Object> result = new HashMap<>();
        List<Tag> tags = tagService.list();
        result.put("code", 1);
        result.put("message", "success");
        result.put("data", tags);
        return result;
    }

    @PostMapping("add-tag")
    public Map<String, Object> addTag(@RequestParam("tagName") String tagName) {
        Map<String, Object> result = new HashMap<>();
        boolean success = tagService.addTag(tagName);
        if (success) {
            result.put("code", 1);
            result.put("message", "success");
        } else {
            result.put("code", 0);
            result.put("message", "标签已存在");
        }
        result.put("data", new HashMap<>());
        return result;
    }

    @DeleteMapping("delete-tag")
    public Map<String, Object> deleteTag(@RequestParam("tagId") Long tagId) {
        Map<String, Object> result = new HashMap<>();
        boolean success = tagService.deleteTag(tagId);
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

    // 管理员更新媒体信息
    @PostMapping("update-media")
    public Map<String, Object> updateMedia(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 从参数中获取媒体ID、标题和描述
            Long id = Long.valueOf(params.get("id").toString());
            String fileTitle = (String) params.get("fileTitle");
            String fileDescription = (String) params.get("fileDescription");
            
            // 检查媒体是否存在
            MediaFile mediaFile = mediaFileService.getById(id);
            if (mediaFile == null) {
                result.put("code", 0);
                result.put("message", "媒体不存在");
                result.put("data", new HashMap<>());
                return result;
            }
            
            // 更新媒体信息
            mediaFile.setTitle(fileTitle);
            mediaFile.setDescription(fileDescription);
            boolean success = mediaFileService.updateById(mediaFile);
            
            if (success) {
                result.put("code", 1);
                result.put("message", "success");
            } else {
                result.put("code", 0);
                result.put("message", "更新失败");
            }
            result.put("data", new HashMap<>());
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "服务器内部错误: " + e.getMessage());
            result.put("data", new HashMap<>());
        }
        return result;
    }
    
    // 管理员删除媒体
    @DeleteMapping("delete-media")
    public Map<String, Object> adminDeleteMedia(@RequestParam("id") Long mediaId) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 管理员删除媒体不需要验证用户ID
            MediaFile mediaFile = mediaFileService.getById(mediaId);
            if (mediaFile == null) {
                result.put("code", 0);
                result.put("message", "媒体不存在");
                result.put("data", new HashMap<>());
                return result;
            }
            
            boolean success = mediaFileService.removeById(mediaId);
            
            if (success) {
                // 删除文件
                File file = new File(mediaFile.getFilePath());
                if (file.exists()) {
                    file.delete();
                }
                result.put("code", 1);
                result.put("message", "success");
            } else {
                result.put("code", 0);
                result.put("message", "删除失败");
            }
            result.put("data", new HashMap<>());
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "服务器内部错误: " + e.getMessage());
            result.put("data", new HashMap<>());
        }
        return result;
    }
}