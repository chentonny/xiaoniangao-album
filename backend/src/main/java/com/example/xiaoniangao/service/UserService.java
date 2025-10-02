package com.example.xiaoniangao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.xiaoniangao.entity.User;

public interface UserService extends IService<User> {

    User login(String userName, String password);

    boolean register(User user);

    User findByUserName(String userName);

    boolean deleteUser(Long userId);
    
    /**
     * 更新用户状态
     * @param userId 用户ID
     * @param status 用户状态（0无效，1有效）
     * @return 是否更新成功
     */
    boolean updateUserStatus(Long userId, Integer status);
    
    /**
     * 更新用户角色
     * @param userId 用户ID
     * @param role 用户角色（"1"管理员，"2"普通用户，"3"访客）
     * @return 是否更新成功
     */
    boolean updateUserRole(Long userId, String role);
    
    /**
     * 根据用户ID获取用户信息
     * @param userId 用户ID
     * @return 用户信息
     */
    User getUserById(Long userId);
}