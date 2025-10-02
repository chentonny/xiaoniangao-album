package com.example.xiaoniangao.service;

import java.awt.image.BufferedImage;

public interface CaptchaService {
    /**
     * 生成验证码图片
     * @return 验证码图片
     */
    BufferedImage generateCaptchaImage();
    
    /**
     * 获取当前生成的验证码文本
     * @return 验证码文本
     */
    String getCaptchaCode();
}