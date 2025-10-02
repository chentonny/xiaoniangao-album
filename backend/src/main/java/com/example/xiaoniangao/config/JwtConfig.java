package com.example.xiaoniangao.config;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.util.Base64;

/**
 * JWT配置类
 */
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

    /**
     * JWT密钥
     */
    private String secret;

    /**
     * JWT过期时间（毫秒）
     */
    private long expiration = 86400000; // 24小时

    /**
     * JWT前缀
     */
    private String tokenPrefix = "Bearer ";

    /**
     * 请求头中的token字段名
     */
    private String headerString = "Authorization";

    private SecretKey secureKey;

    @PostConstruct
    public void init() {
        // 如果配置了secret，则使用配置的值；否则生成随机密钥
        if (secret != null && !secret.isEmpty()) {
            // 确保密钥长度足够（HS512算法推荐至少64字节）
            byte[] keyBytes = secret.getBytes();
            if (keyBytes.length < 64) {
                // 如果密钥太短，进行填充
                byte[] paddedKey = new byte[64];
                System.arraycopy(keyBytes, 0, paddedKey, 0, Math.min(keyBytes.length, paddedKey.length));
                secureKey = Keys.hmacShaKeyFor(paddedKey);
            } else {
                secureKey = Keys.hmacShaKeyFor(keyBytes);
            }
        } else {
            // 使用JWT库提供的安全密钥生成方法
            secureKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        }
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpiration() {
        return expiration;
    }

    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }

    public String getTokenPrefix() {
        return tokenPrefix;
    }

    public void setTokenPrefix(String tokenPrefix) {
        this.tokenPrefix = tokenPrefix;
    }

    public String getHeaderString() {
        return headerString;
    }

    public void setHeaderString(String headerString) {
        this.headerString = headerString;
    }

    public SecretKey getSecureKey() {
        return secureKey;
    }
}