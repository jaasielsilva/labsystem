package com.jaasielsilva.labsystem.config;

import com.jaasielsilva.labsystem.features.auth.entity.Perfil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private static final String CLAIM_PERFIL = "perfil";
    private static final String CLAIM_TYPE = "type";
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";

    private final JwtProperties properties;
    private final SecretKey secretKey;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.secretKey = Keys.hmacShaKeyFor(resolveSecretBytes(properties.secret()));
    }

    public String generateAccessToken(String email, Perfil perfil) {
        return buildToken(email, perfil, properties.accessExpirationMs(), TYPE_ACCESS);
    }

    public String generateRefreshToken(String email, Perfil perfil) {
        return buildToken(email, perfil, properties.refreshExpirationMs(), TYPE_REFRESH);
    }

    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean isAccessToken(String token) {
        return TYPE_ACCESS.equals(parseClaims(token).get(CLAIM_TYPE, String.class));
    }

    public boolean isRefreshToken(String token) {
        return TYPE_REFRESH.equals(parseClaims(token).get(CLAIM_TYPE, String.class));
    }

    public boolean isTokenValid(String token, String email) {
        try {
            Claims claims = parseClaims(token);
            return email.equals(claims.getSubject()) && !isExpired(claims);
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    private String buildToken(String email, Perfil perfil, long expirationMs, String type) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(email)
                .claims(Map.of(CLAIM_PERFIL, perfil.name(), CLAIM_TYPE, type))
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean isExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }

    private byte[] resolveSecretBytes(String secret) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length >= 32) {
            return keyBytes;
        }
        return Decoders.BASE64.decode(
                java.util.Base64.getEncoder().encodeToString(keyBytes)
        );
    }
}
