package com.example.xiaoniangao.controller;

import com.example.xiaoniangao.service.CaptchaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/")
public class CaptchaController {

    @Autowired
    private CaptchaService captchaService;

    /**
     * 生成验证码图片
     */
    @GetMapping("captcha")
    public void generateCaptcha(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 设置响应头
        response.setContentType("image/jpeg");
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        
        // 生成验证码图片和文本
        BufferedImage image = captchaService.generateCaptchaImage();
        String captchaCode = captchaService.getCaptchaCode();
        
        // 将验证码文本存储在session中，用于后续验证
        HttpSession session = request.getSession();
        session.setAttribute("captchaCode", captchaCode.toLowerCase()); // 存储为小写以便不区分大小写验证
        
        // 输出验证码图片
        ServletOutputStream out = response.getOutputStream();
        ImageIO.write(image, "jpeg", out);
        out.flush();
        out.close();
    }
    
    /**
     * 验证用户输入的验证码
     */
    @PostMapping("captcha/verify")
    public Map<String, Object> verifyCaptcha(@RequestBody Map<String, String> requestBody, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String userInput = requestBody.get("captcha");
            
            if (userInput == null || userInput.isEmpty()) {
                result.put("code", 0);
                result.put("message", "验证码不能为空");
                return result;
            }
            
            // 从session中获取验证码
            HttpSession session = request.getSession(false);
            if (session == null) {
                result.put("code", 0);
                result.put("message", "会话已过期，请刷新页面");
                return result;
            }
            
            String captchaCode = (String) session.getAttribute("captchaCode");
            if (captchaCode == null) {
                result.put("code", 0);
                result.put("message", "验证码已过期，请刷新验证码");
                return result;
            }
            
            // 验证用户输入的验证码是否正确
            if (captchaCode.equals(userInput.toLowerCase())) {
                result.put("code", 1);
                result.put("message", "验证成功");
            } else {
                result.put("code", 0);
                result.put("message", "验证码错误，请重新输入");
            }
        } catch (Exception e) {
            result.put("code", 0);
            result.put("message", "验证失败，服务器内部错误");
        }
        
        return result;
    }
}