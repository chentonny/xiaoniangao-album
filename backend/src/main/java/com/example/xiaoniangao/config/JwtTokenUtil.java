package com.example.xiaoniangao.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Token工具类
 */
@Component
public class JwtTokenUtil {

    @Autowired
    private JwtConfig jwtConfig;

    /**
     * 生成Token
     */
    public String generateToken(String username, Long userId, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", username);
        claims.put("userId", userId);
        // 将String类型的role转换为Integer存储在token中
        try {
            claims.put("role", Integer.parseInt(role));
        } catch (NumberFormatException e) {
            // 如果转换失败，使用默认值3（访客）
            claims.put("role", 3);
        }
        claims.put("created", new Date());

        SecretKey secretKey = jwtConfig.getSecureKey();
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(generateExpirationDate())
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 从Token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        String username;
        try {
            final Claims claims = getClaimsFromToken(token);
            username = claims.getSubject();
        } catch (Exception e) {
            username = null;
        }
        return username;
    }

    /**
     * 从Token中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        Long userId;
        try {
            final Claims claims = getClaimsFromToken(token);
            userId = claims.get("userId", Long.class);
        } catch (Exception e) {
            userId = null;
        }
        return userId;
    }

    /**
     * 从Token中获取用户角色
     */
    public Integer getRoleFromToken(String token) {
        Integer role;
        try {
            final Claims claims = getClaimsFromToken(token);
            role = claims.get("role", Integer.class);
        } catch (Exception e) {
            role = null;
        }
        return role;
    }

    /**
     * 验证Token是否过期
     */
    public Boolean isTokenExpired(String token) {
        try {
            final Claims claims = getClaimsFromToken(token);
            final Date expiration = claims.getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 刷新Token
     */
    public String refreshToken(String token) {
        String refreshedToken;
        try {
            final Claims claims = getClaimsFromToken(token);
            claims.put("created", new Date());
            SecretKey secretKey = jwtConfig.getSecureKey();
            refreshedToken = Jwts.builder()
                    .setClaims(claims)
                    .setExpiration(generateExpirationDate())
                    .signWith(secretKey, SignatureAlgorithm.HS512)
                    .compact();
        } catch (Exception e) {
            refreshedToken = null;
        }
        return refreshedToken;
    }

    /**
     * 验证Token
     */
    public Boolean validateToken(String token, String username) {
        final String usernameFromToken = getUsernameFromToken(token);
        return (usernameFromToken.equals(username) && !isTokenExpired(token));
    }

    /**
     * 生成过期时间
     */
    private Date generateExpirationDate() {
        return new Date(System.currentTimeMillis() + jwtConfig.getExpiration());
    }

    /**
     * 从Token中获取Claims
     */
    private Claims getClaimsFromToken(String token) {
        Claims claims;
        try {
            SecretKey secretKey = jwtConfig.getSecureKey();
            claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            claims = null;
        }
        return claims;
    }
}