package com.example.xiaoniangao.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT认证过滤器
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtConfig jwtConfig;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        // 获取请求头中的Authorization字段
        String header = request.getHeader(jwtConfig.getHeaderString());
        String username = null;
        String authToken = null;

        // 检查Authorization头是否包含Bearer前缀
        if (header != null && header.startsWith(jwtConfig.getTokenPrefix())) {
            // 提取token（去掉Bearer前缀）
            authToken = header.substring(jwtConfig.getTokenPrefix().length());
            try {
                username = jwtTokenUtil.getUsernameFromToken(authToken);
            } catch (Exception e) {
                logger.error("提取用户名失败: " + e.getMessage());
            }
        }

        // 如果token有效，设置用户信息到request属性中供后续处理
        if (username != null && jwtTokenUtil.validateToken(authToken, username)) {
            // 在实际应用中，这里可以从数据库获取用户信息，然后设置到Security Context中
            // 由于当前项目没有集成Spring Security，我们简单地将用户信息设置到request属性中
            request.setAttribute("username", username);
            request.setAttribute("userId", jwtTokenUtil.getUserIdFromToken(authToken));
            request.setAttribute("role", jwtTokenUtil.getRoleFromToken(authToken));
        }

        // 继续过滤器链
        filterChain.doFilter(request, response);
    }
}