package com.zxw.treehole.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 签发参数（生产环境务必使用环境变量覆盖 secret）
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    /** HS256 密钥，长度建议 ≥ 256 bit */
    private String secret = "treehole-dev-secret-change-me-in-production-min-32-chars!!";
    /** 过期时间（毫秒），默认 24h */
    private long expirationMs = 86400000L;
    /** 请求头名称 */
    private String header = "Authorization";
    /** Token 前缀 */
    private String prefix = "Bearer ";
}
