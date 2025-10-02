package com.example.xiaoniangao.controller;

import com.example.xiaoniangao.config.JwtTokenUtil;
import com.example.xiaoniangao.entity.User;
import com.example.xiaoniangao.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    
    @Autowired
    private HttpServletRequest request;

    @PostMapping("login")
    public Map<String, Object> login(@RequestBody Map<String, String> loginInfo) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 检查请求参数
            if (loginInfo == null || loginInfo.isEmpty()) {
                result.put("code", 0);
                result.put("message", "登录参数不能为空");
                result.put("data", new HashMap<>());
                return result;
            }
            
            String userName = loginInfo.get("username");
            String password = loginInfo.get("password");
            String captcha = loginInfo.get("captcha");
            
            if (userName == null || userName.isEmpty() || password == null || password.isEmpty()) {
                result.put("code", 0);
                result.put("message", "用户名或密码不能为空");
                result.put("data", new HashMap<>());
                return result;
            }
            
            // 验证码验证
            if (captcha == null || captcha.isEmpty()) {
                result.put("code", 0);
                result.put("message", "验证码不能为空");
                result.put("data", new HashMap<>());
                return result;
            }
            
            // 从session中获取验证码
            HttpSession session = request.getSession(false);
            if (session == null) {
                result.put("code", 0);
                result.put("message", "会话已过期，请刷新页面");
                result.put("data", new HashMap<>());
                return result;
            }
            
            String sessionCaptcha = (String) session.getAttribute("captchaCode");
            if (sessionCaptcha == null) {
                result.put("code", 0);
                result.put("message", "验证码已过期，请刷新验证码");
                result.put("data", new HashMap<>());
                return result;
            }
            
            // 验证用户输入的验证码是否正确
            if (!sessionCaptcha.equals(captcha.toLowerCase())) {
                // 验证失败也要清除session中的验证码，防止重复尝试
                session.removeAttribute("captchaCode");
                result.put("code", 0);
                result.put("message", "验证码错误，请重新输入");
                result.put("data", new HashMap<>());
                return result;
            }
            
            // 验证成功后，移除session中的验证码，防止重复使用
            session.removeAttribute("captchaCode");
            
            // 调用服务层验证用户
            User user = userService.login(userName, password);
            
            if (user != null) {
                // 生成JWT token
                String token = jwtTokenUtil.generateToken(user.getUserName(), user.getId(), user.getRole());
                
                Map<String, Object> data = new HashMap<>();
                data.put("userID", user.getId());
                data.put("userName", user.getUserName());
                // 添加昵称信息
                if (user.getNickname() != null && !user.getNickname().isEmpty()) {
                    data.put("nickname", user.getNickname());
                } else {
                    data.put("nickname", user.getUserName());
                }
                // 添加默认头像
                data.put("avatar", "/static/images/user-avatar.svg");
                data.put("role", user.getRole());
                data.put("token", token);
                
                result.put("code", 1);
                result.put("message", "success");
                result.put("data", data);
            } else {
                result.put("code", 0);
                result.put("message", "用户名或密码错误");
                result.put("data", new HashMap<>());
            }
        } catch (Exception e) {
            result.put("code", 0);
            result.put("message", "登录失败，服务器内部错误");
            result.put("data", new HashMap<>());
        }
        
        return result;
    }

    @PostMapping("register")
    public Map<String, Object> register(@RequestBody User user) {
        
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = userService.register(user);
            if (success) {
                result.put("code", 1);
                result.put("message", "success");
                result.put("data", new HashMap<>());
            } else {
                result.put("code", 0);
                result.put("message", "注册失败，用户名已存在");
                result.put("data", new HashMap<>());
            }
        } catch (Exception e) {
            result.put("code", 0);
            result.put("message", "注册失败，服务器内部错误");
            result.put("data", new HashMap<>());
        }
        return result;
    }
    
    /**
     * 获取用户详细信息接口
     * @return 用户详细信息
     */
    @GetMapping("user/info")
    public Map<String, Object> getUserInfo() {
        Map<String, Object> result = new HashMap<>();
        try {
            // 从请求头中获取token
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            // 从token中获取用户ID
            Long userId = jwtTokenUtil.getUserIdFromToken(token);
            
            if (userId != null) {
                // 查询用户详细信息
                User user = userService.getUserById(userId);
                if (user != null) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("id", user.getId());
                    data.put("userName", user.getUserName());
                    data.put("nickname", user.getNickname());
                    data.put("email", user.getEmail());
                    data.put("phone", user.getPhone());
                    data.put("role", user.getRole());
                    data.put("status", user.getStatus());
                    data.put("createTime", user.getCreateTime());
                    
                    result.put("code", 1);
                    result.put("message", "success");
                    result.put("data", data);
                } else {
                    result.put("code", 0);
                    result.put("message", "用户不存在");
                    result.put("data", new HashMap<>());
                }
            } else {
                result.put("code", 0);
                result.put("message", "无效的token");
                result.put("data", new HashMap<>());
            }
        } catch (Exception e) {
            result.put("code", 0);
            result.put("message", "获取用户信息失败，服务器内部错误");
            result.put("data", new HashMap<>());
        }
        return result;
    }
    
    /**
     * 通过用户ID获取用户基本信息（仅包含公开信息，如用户名）
     * @param userId 用户ID
     * @return 用户基本信息
     */
    @GetMapping("user/{userId}")
    public Map<String, Object> getUserBasicInfo(@PathVariable Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 检查参数
            if (userId == null) {
                result.put("code", 0);
                result.put("message", "用户ID不能为空");
                result.put("data", new HashMap<>());
                return result;
            }
            
            // 查询用户基本信息
            User user = userService.getUserById(userId);
            if (user != null) {
                Map<String, Object> data = new HashMap<>();
                // 只返回非敏感的公开信息
                data.put("id", user.getId());
                data.put("userName", user.getUserName());
                data.put("nickname", user.getNickname());
                data.put("role", user.getRole());
                
                result.put("code", 1);
                result.put("message", "success");
                result.put("data", data);
            } else {
                result.put("code", 0);
                result.put("message", "用户不存在");
                result.put("data", new HashMap<>());
            }
        } catch (Exception e) {
            result.put("code", 0);
            result.put("message", "获取用户信息失败，服务器内部错误");
            result.put("data", new HashMap<>());
        }
        return result;
    }
    
    /**
     * 更新用户个人信息接口
     * @param user 用户信息
     * @return 操作结果
     */
    @PostMapping("user/update")
    public Map<String, Object> updateUserInfo(@RequestBody User user) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 从请求头中获取token
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            // 从token中获取用户ID
            Long userId = jwtTokenUtil.getUserIdFromToken(token);
            
            if (userId != null) {
                // 确保用户只能更新自己的信息
                user.setId(userId);
                
                // 只允许更新部分字段，防止越权修改
                User existingUser = userService.getUserById(userId);
                if (existingUser != null) {
                    // 保留不可修改的字段
                    user.setUserName(existingUser.getUserName());
                    user.setPassword(existingUser.getPassword());
                    user.setRole(existingUser.getRole());
                    user.setStatus(existingUser.getStatus());
                    user.setCreateTime(existingUser.getCreateTime());
                    
                    // 使用MyBatis-Plus的updateById方法更新用户信息
                    boolean success = userService.updateById(user);
                    
                    if (success) {
                        result.put("code", 1);
                        result.put("message", "success");
                        result.put("data", new HashMap<>());
                    } else {
                        result.put("code", 0);
                        result.put("message", "更新失败，请稍后重试");
                        result.put("data", new HashMap<>());
                    }
                } else {
                    result.put("code", 0);
                    result.put("message", "用户不存在");
                    result.put("data", new HashMap<>());
                }
            } else {
                result.put("code", 0);
                result.put("message", "无效的token");
                result.put("data", new HashMap<>());
            }
        } catch (Exception e) {
            result.put("code", 0);
            result.put("message", "更新失败，服务器内部错误");
            result.put("data", new HashMap<>());
        }
        return result;
    }

    // 测试方法已移除
    
    /**
     * 更新用户状态接口（管理员使用）
     * @param userId 用户ID
     * @param status 新的用户状态（0无效，1有效）
     * @return 操作结果
     */
    @PostMapping("admin/user/status/{userId}")
    public Map<String, Object> updateUserStatus(@PathVariable Long userId, @RequestParam Integer status) {
        
        Map<String, Object> result = new HashMap<>();
        try {
            // 简单的权限检查（实际项目中应该有更完善的权限管理）
            if (status != 0 && status != 1) {
                result.put("code", 0);
                result.put("message", "状态值无效，只能是0或1");
                result.put("data", new HashMap<>());
                return result;
            }
            
            boolean success = userService.updateUserStatus(userId, status);
            if (success) {
                result.put("code", 1);
                result.put("message", "success");
                result.put("data", Map.of("userId", userId, "status", status));
            } else {
                result.put("code", 0);
                result.put("message", "更新失败，用户不存在或发生错误");
                result.put("data", new HashMap<>());
            }
        } catch (Exception e) {
            result.put("code", 0);
            result.put("message", "更新失败，服务器内部错误");
            result.put("data", new HashMap<>());
        }
        return result;
    }
    
    /**
     * 更新用户角色接口（管理员使用）
     * @param userId 用户ID
     * @param role 新的用户角色（1管理员，2普通用户，3访客）
     * @return 操作结果
     */
    @PostMapping("admin/user/role/{userId}")
    public Map<String, Object> updateUserRole(@PathVariable Long userId, @RequestParam Integer role) {
        
        Map<String, Object> result = new HashMap<>();
        try {
            // 简单的权限检查（实际项目中应该有更完善的权限管理）
            if (role != 1 && role != 2 && role != 3) {
                result.put("code", 0);
                result.put("message", "角色值无效，只能是1、2或3");
                result.put("data", new HashMap<>());
                return result;
            }
            
            // 将Integer类型的role转换为String类型再传递给userService
            boolean success = userService.updateUserRole(userId, String.valueOf(role));
            if (success) {
                result.put("code", 1);
                result.put("message", "success");
                result.put("data", Map.of("userId", userId, "role", role));
            } else {
                result.put("code", 0);
                result.put("message", "更新失败，用户不存在或发生错误");
                result.put("data", new HashMap<>());
            }
        } catch (Exception e) {
            result.put("code", 0);
            result.put("message", "更新失败，服务器内部错误");
            result.put("data", new HashMap<>());
        }
        return result;
    }
}