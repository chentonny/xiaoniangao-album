package com.example.xiaoniangao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.xiaoniangao.entity.User;
import com.example.xiaoniangao.mapper.UserMapper;
import com.example.xiaoniangao.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public User login(String userName, String password) {
        try {
            User user = userMapper.findByUserName(userName);
            
            if (user != null) {
                // 简单的密码加密验证
                String encryptedPassword = DigestUtils.md5DigestAsHex(password.getBytes());
                
                if (encryptedPassword.equals(user.getPassword())) {
                    return user;
                }
            }
        } catch (Exception e) {
            // 异常处理已简化
        }
        
        return null;
    }

    @Override
    public boolean register(User user) {
        try {
            // 检查用户名是否已存在
            User existingUser = userMapper.findByUserName(user.getUserName());
            if (existingUser != null) {
                return false;
            }
            
            // 密码加密
            String encryptedPassword = DigestUtils.md5DigestAsHex(user.getPassword().getBytes());
            user.setPassword(encryptedPassword);
            
            // 默认角色为访客
            user.setRole("3");
            
            // 保存用户
            boolean result = this.save(user);
            return result;
        } catch (Exception e) {
            // 异常处理已简化
            return false;
        }
    }

    @Override
    public User findByUserName(String userName) {
        try {
            User user = userMapper.findByUserName(userName);
            return user;
        } catch (Exception e) {
            // 异常处理已简化
            return null;
        }
    }

    @Override
    public boolean deleteUser(Long userId) {
        return this.removeById(userId);
    }

    @Override
    public boolean updateUserStatus(Long userId, Integer status) {
        try {
            User user = this.getById(userId);
            if (user != null) {
                user.setStatus(status);
                boolean result = this.updateById(user);
                return result;
            } else {
                return false;
            }
        } catch (Exception e) {
            // 异常处理已简化
            return false;
        }
    }

    @Override
    public boolean updateUserRole(Long userId, String role) {
        try {
            User user = this.getById(userId);
            if (user != null) {
                // 验证角色值是否有效
                if (!"1".equals(role) && !"2".equals(role) && !"3".equals(role)) {
                    return false;
                }
                
                user.setRole(role);
                boolean result = this.updateById(user);
                return result;
            } else {
                return false;
            }
        } catch (Exception e) {
            // 异常处理已简化
            return false;
        }
    }
    
    @Override
    public User getUserById(Long userId) {
        try {
            User user = this.getById(userId);
            return user;
        } catch (Exception e) {
            // 异常处理已简化
            return null;
        }
    }
}