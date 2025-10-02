package com.example.xiaoniangao.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 允许指定源的跨域请求
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5173", "http://localhost:5174")  // 同时支持5173和5174端口
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册JWT认证过滤器，排除不需要认证的路径
        registry.addInterceptor(new JwtAuthenticationInterceptor(jwtAuthenticationFilter))
                .addPathPatterns("/**")
                .excludePathPatterns("/api/login", "/api/register"); // 登录和注册接口不需要认证
    }

    /**
     * 适配OncePerRequestFilter到Spring MVC的Interceptor
     */
    private static class JwtAuthenticationInterceptor implements org.springframework.web.servlet.HandlerInterceptor {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;

        public JwtAuthenticationInterceptor(JwtAuthenticationFilter jwtAuthenticationFilter) {
            this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        }

        @Override
        public boolean preHandle(javax.servlet.http.HttpServletRequest request, 
                                 javax.servlet.http.HttpServletResponse response, 
                                 Object handler) throws Exception {
            // 调用过滤器的doFilterInternal方法
            javax.servlet.FilterChain filterChain = new javax.servlet.FilterChain() {
                @Override
                public void doFilter(javax.servlet.ServletRequest request, javax.servlet.ServletResponse response) {
                    // 空实现，因为我们只需要执行过滤器的前置逻辑
                }
            };
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
            return true; // 总是返回true，让请求继续处理
        }
    }
}