package com.zxw.treehole.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * JWT 生成与解析
 */
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtProperties props;

    private SecretKey key() {
        byte[] bytes = props.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(bytes);
    }

    public String createToken(Long userId, String username, List<String> roles) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + props.getExpirationMs());
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(exp)
                .signWith(key())
                .compact();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> parse(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Map.of(
                "userId", Long.parseLong(claims.getSubject()),
                "username", claims.get("username", String.class),
                "roles", claims.get("roles", List.class) == null ? List.of() : claims.get("roles", List.class)
        );
    }
}
