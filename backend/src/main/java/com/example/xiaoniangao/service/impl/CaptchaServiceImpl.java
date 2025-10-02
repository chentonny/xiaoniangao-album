package com.example.xiaoniangao.service.impl;

import com.example.xiaoniangao.service.CaptchaService;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

@Service
public class CaptchaServiceImpl implements CaptchaService {

    private static final int WIDTH = 120; // 验证码图片宽度
    private static final int HEIGHT = 40; // 验证码图片高度
    private static final int CODE_LENGTH = 4; // 验证码长度
    private static final String CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    
    private String currentCaptchaCode;
    private final Random random = new Random();
    
    @Override
    public BufferedImage generateCaptchaImage() {
        // 创建图片缓冲区
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        
        // 设置背景色
        g.setColor(getRandomColor(200, 250));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        
        // 设置字体
        Font font = new Font("Arial", Font.BOLD, 28);
        g.setFont(font);
        
        // 生成随机干扰线
        drawRandomLines(g, 10);
        
        // 生成随机点
        drawRandomPoints(g, 100);
        
        // 生成验证码文本
        StringBuilder sb = new StringBuilder();
        // 调整字符间距，确保所有字符都在图片范围内
        int startX = 15; // 起始X坐标
        int y = 30; // Y坐标
        int charSpacing = 25; // 字符间距
        
        for (int i = 0; i < CODE_LENGTH; i++) {
            char c = CHARS.charAt(random.nextInt(CHARS.length()));
            sb.append(c);
            
            // 绘制单个字符，使用简单的位置偏移而不是旋转
            g.setColor(getRandomColor(40, 150));
            
            // 添加轻微的垂直偏移，增加验证码的复杂性
            int yOffset = random.nextInt(6) - 3; // -3 到 3 的随机偏移
            g.drawString(String.valueOf(c), startX + i * charSpacing, y + yOffset);
        }
        
        // 保存当前生成的验证码文本
        currentCaptchaCode = sb.toString();
        
        // 释放资源
        g.dispose();
        
        return image;
    }
    
    @Override
    public String getCaptchaCode() {
        return currentCaptchaCode;
    }
    
    /**
     * 获取随机颜色
     */
    private Color getRandomColor(int min, int max) {
        return new Color(min + random.nextInt(max - min),
                min + random.nextInt(max - min),
                min + random.nextInt(max - min));
    }
    
    /**
     * 绘制随机干扰线
     */
    private void drawRandomLines(Graphics g, int count) {
        for (int i = 0; i < count; i++) {
            g.setColor(getRandomColor(160, 200));
            int x1 = random.nextInt(WIDTH);
            int y1 = random.nextInt(HEIGHT);
            int x2 = random.nextInt(WIDTH);
            int y2 = random.nextInt(HEIGHT);
            g.drawLine(x1, y1, x2, y2);
        }
    }
    
    /**
     * 绘制随机点
     */
    private void drawRandomPoints(Graphics g, int count) {
        for (int i = 0; i < count; i++) {
            g.setColor(getRandomColor(160, 200));
            int x = random.nextInt(WIDTH);
            int y = random.nextInt(HEIGHT);
            g.drawOval(x, y, 1, 1);
        }
    }
}