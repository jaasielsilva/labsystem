package com.jaasielsilva.labsystem.config;

import com.jaasielsilva.labsystem.common.AccessScope;
import com.jaasielsilva.labsystem.common.JwtTokenContext;
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
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    private static final String CLAIM_PERFIL = "perfil";
    private static final String CLAIM_EMPRESA_ID = "empresaId";
    private static final String CLAIM_SCOPE = "scope";
    private static final String CLAIM_ACTING_EMPRESA_ID = "actingEmpresaId";
    private static final String CLAIM_ACTING_EMPRESA_NOME = "actingEmpresaNome";
    private static final String CLAIM_TYPE = "type";
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";

    private final JwtProperties properties;
    private final SecretKey secretKey;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.secretKey = Keys.hmacShaKeyFor(resolveSecretBytes(properties.secret()));
    }

    public String generateAccessToken(String email, Perfil perfil, Long empresaId) {
        return buildToken(email, perfil, empresaId, AccessScope.fromPerfil(perfil), null, null,
                properties.accessExpirationMs(), TYPE_ACCESS);
    }

    public String generateRefreshToken(String email, Perfil perfil, Long empresaId) {
        return buildToken(email, perfil, empresaId, AccessScope.fromPerfil(perfil), null, null,
                properties.refreshExpirationMs(), TYPE_REFRESH);
    }

    public String generateImpersonationAccessToken(
            String email,
            Perfil perfil,
            Long platformEmpresaId,
            Long actingEmpresaId,
            String actingEmpresaNome) {
        return buildToken(email, perfil, platformEmpresaId, AccessScope.TENANT_IMPERSONATION,
                actingEmpresaId, actingEmpresaNome, properties.accessExpirationMs(), TYPE_ACCESS);
    }

    public String generateImpersonationRefreshToken(
            String email,
            Perfil perfil,
            Long platformEmpresaId,
            Long actingEmpresaId,
            String actingEmpresaNome) {
        return buildToken(email, perfil, platformEmpresaId, AccessScope.TENANT_IMPERSONATION,
                actingEmpresaId, actingEmpresaNome, properties.refreshExpirationMs(), TYPE_REFRESH);
    }

    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    public Long extractEmpresaId(String token) {
        return readLongClaim(parseClaims(token), CLAIM_EMPRESA_ID);
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

    public JwtTokenContext readTokenContext(String token) {
        Claims claims = parseClaims(token);
        Perfil perfil = Perfil.valueOf(claims.get(CLAIM_PERFIL, String.class));
        AccessScope scope = AccessScope.valueOf(claims.get(CLAIM_SCOPE, String.class));

        return new JwtTokenContext(
                claims.getSubject(),
                perfil,
                readLongClaim(claims, CLAIM_EMPRESA_ID),
                scope,
                readLongClaim(claims, CLAIM_ACTING_EMPRESA_ID),
                claims.get(CLAIM_ACTING_EMPRESA_NOME, String.class),
                claims.get(CLAIM_TYPE, String.class)
        );
    }

    private String buildToken(
            String email,
            Perfil perfil,
            Long empresaId,
            AccessScope scope,
            Long actingEmpresaId,
            String actingEmpresaNome,
            long expirationMs,
            String type) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_PERFIL, perfil.name());
        claims.put(CLAIM_TYPE, type);
        claims.put(CLAIM_EMPRESA_ID, empresaId);
        claims.put(CLAIM_SCOPE, scope.name());

        if (scope == AccessScope.TENANT_IMPERSONATION) {
            claims.put(CLAIM_ACTING_EMPRESA_ID, actingEmpresaId);
            claims.put(CLAIM_ACTING_EMPRESA_NOME, actingEmpresaNome);
        }

        return Jwts.builder()
                .subject(email)
                .claims(claims)
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

    private Long readLongClaim(Claims claims, String key) {
        Object value = claims.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(value.toString());
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
