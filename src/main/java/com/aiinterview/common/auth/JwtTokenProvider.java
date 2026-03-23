package com.aiinterview.common.auth;

import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class JwtTokenProvider {

    private static final String HMAC_ALG = "HmacSHA256";

    private final ObjectMapper objectMapper;
    private final byte[] secretBytes;
    private final long accessTokenExpiresIn;

    public JwtTokenProvider(
            ObjectMapper objectMapper,
            @Value("${auth.jwt.secret:change-this-secret-in-production}") String secret,
            @Value("${auth.jwt.access-token-expires-in:7200}") long accessTokenExpiresIn) {
        if (!StringUtils.hasText(secret)) {
            throw new IllegalArgumentException("auth.jwt.secret must not be blank");
        }
        this.objectMapper = objectMapper;
        this.secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        this.accessTokenExpiresIn = accessTokenExpiresIn;
    }

    public String generateAccessToken(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("userId must be positive");
        }
        long now = Instant.now().getEpochSecond();

        Map<String, Object> header = new LinkedHashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", userId);
        payload.put("iat", now);
        payload.put("exp", now + accessTokenExpiresIn);

        String headerPart = toBase64Url(toJson(header));
        String payloadPart = toBase64Url(toJson(payload));
        String signingInput = headerPart + "." + payloadPart;
        String signature = sign(signingInput);
        return signingInput + "." + signature;
    }

    public Long parseUserId(String token) {
        if (!StringUtils.hasText(token)) {
            throw new AuthException("missing access token");
        }

        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new AuthException("invalid access token");
        }

        String signingInput = parts[0] + "." + parts[1];
        String expectedSignature = sign(signingInput);
        if (!MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.US_ASCII),
                parts[2].getBytes(StandardCharsets.US_ASCII))) {
            throw new AuthException("invalid access token");
        }

        try {
            Map<String, Object> payload = objectMapper.readValue(fromBase64Url(parts[1]), Map.class);
            Long exp = asLong(payload.get("exp"));
            long now = Instant.now().getEpochSecond();
            if (exp == null || exp <= now) {
                throw new AuthException("access token expired");
            }

            Long userId = asLong(payload.get("sub"));
            if (userId == null || userId <= 0) {
                throw new AuthException("invalid access token");
            }
            return userId;
        } catch (IllegalArgumentException | IOException ex) {
            throw new AuthException("invalid access token");
        }
    }

    public long getAccessTokenExpiresIn() {
        return accessTokenExpiresIn;
    }

    private String sign(String signingInput) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALG);
            mac.init(new SecretKeySpec(secretBytes, HMAC_ALG));
            byte[] signature = mac.doFinal(signingInput.getBytes(StandardCharsets.UTF_8));
            return toBase64Url(signature);
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            throw new IllegalStateException("failed to sign token", ex);
        }
    }

    private byte[] toJson(Map<String, Object> value) {
        try {
            return objectMapper.writeValueAsBytes(value);
        } catch (IOException ex) {
            throw new IllegalStateException("failed to serialize token", ex);
        }
    }

    private String toBase64Url(byte[] value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    private byte[] fromBase64Url(String value) {
        return Base64.getUrlDecoder().decode(value);
    }

    private Long asLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text && StringUtils.hasText(text)) {
            try {
                return Long.parseLong(text);
            } catch (NumberFormatException ex) {
                return null;
            }
        }
        return null;
    }
}
