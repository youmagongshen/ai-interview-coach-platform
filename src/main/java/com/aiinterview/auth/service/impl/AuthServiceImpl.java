package com.aiinterview.auth.service.impl;

import com.aiinterview.auth.dto.AuthTokenResp;
import com.aiinterview.auth.dto.LoginReq;
import com.aiinterview.auth.dto.RegisterReq;
import com.aiinterview.auth.dto.RegisterResp;
import com.aiinterview.auth.entity.RefreshTokenEntity;
import com.aiinterview.auth.mapper.RefreshTokenMapper;
import com.aiinterview.auth.service.AuthService;
import com.aiinterview.common.auth.JwtTokenProvider;
import com.aiinterview.user.entity.UserEntity;
import com.aiinterview.user.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final RefreshTokenMapper refreshTokenMapper;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public RegisterResp register(RegisterReq req) {
        boolean usernameExists = userMapper.selectCount(
                new LambdaQueryWrapper<UserEntity>().eq(UserEntity::getUsername, req.getUsername())) > 0;
        boolean phoneExists = userMapper.selectCount(
                new LambdaQueryWrapper<UserEntity>().eq(UserEntity::getPhone, req.getPhone())) > 0;
        boolean emailExists = userMapper.selectCount(
                new LambdaQueryWrapper<UserEntity>().eq(UserEntity::getEmail, req.getEmail())) > 0;

        if (usernameExists || phoneExists || emailExists) {
            return null;
        }

        UserEntity user = new UserEntity();
        user.setUsername(req.getUsername());
        user.setPasswordHash(sha256(req.getPassword()));
        user.setPhone(req.getPhone());
        user.setEmail(req.getEmail());
        user.setTermsAccepted(req.getTermsAccepted() != null && req.getTermsAccepted());
        user.setTermsVersion(req.getTermsVersion());
        user.setTermsAcceptedAt(Boolean.TRUE.equals(user.getTermsAccepted()) ? LocalDateTime.now() : null);
        user.setStatus(1);
        userMapper.insert(user);
        return new RegisterResp(user.getId());
    }

    @Override
    public AuthTokenResp login(LoginReq req) {
        UserEntity user = userMapper.selectOne(
                new LambdaQueryWrapper<UserEntity>()
                        .eq(UserEntity::getUsername, req.getUsername())
                        .last("limit 1"));
        if (user == null || user.getStatus() == null || user.getStatus() != 1) {
            return null;
        }
        if (!sha256(req.getPassword()).equalsIgnoreCase(user.getPasswordHash())) {
            return null;
        }
        return issueTokens(user.getId(), req.getRememberDays(), req.getDeviceLabel());
    }

    @Override
    public AuthTokenResp refresh(String refreshToken) {
        if (!StringUtils.hasText(refreshToken)) {
            return null;
        }
        String hash = sha256(refreshToken);
        RefreshTokenEntity token = refreshTokenMapper.selectOne(
                new LambdaQueryWrapper<RefreshTokenEntity>()
                        .eq(RefreshTokenEntity::getTokenHash, hash)
                        .last("limit 1"));
        if (token == null || token.getRevokedAt() != null || token.getExpiresAt() == null
                || token.getExpiresAt().isBefore(LocalDateTime.now())) {
            return null;
        }

        refreshTokenMapper.update(
                null,
                new LambdaUpdateWrapper<RefreshTokenEntity>()
                        .set(RefreshTokenEntity::getRevokedAt, LocalDateTime.now())
                        .eq(RefreshTokenEntity::getId, token.getId())
                        .isNull(RefreshTokenEntity::getRevokedAt));

        return issueTokens(token.getUserId(), token.getRememberDays(), token.getDeviceLabel());
    }

    @Override
    public boolean logout(String refreshToken) {
        if (!StringUtils.hasText(refreshToken)) {
            return false;
        }
        String hash = sha256(refreshToken);
        RefreshTokenEntity token = refreshTokenMapper.selectOne(
                new LambdaQueryWrapper<RefreshTokenEntity>()
                        .eq(RefreshTokenEntity::getTokenHash, hash)
                        .last("limit 1"));
        if (token == null) {
            return false;
        }
        return refreshTokenMapper.update(
                null,
                new LambdaUpdateWrapper<RefreshTokenEntity>()
                        .set(RefreshTokenEntity::getRevokedAt, LocalDateTime.now())
                        .eq(RefreshTokenEntity::getId, token.getId())
                        .isNull(RefreshTokenEntity::getRevokedAt)) > 0;
    }

    private AuthTokenResp issueTokens(Long userId, Integer rememberDays, String deviceLabel) {
        int days = rememberDays == null || rememberDays <= 0 ? 7 : Math.min(30, rememberDays);

        String accessToken = jwtTokenProvider.generateAccessToken(userId);
        String refreshToken = UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "");

        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.setUserId(userId);
        entity.setTokenHash(sha256(refreshToken));
        entity.setDeviceLabel(deviceLabel);
        entity.setRememberDays(days);
        entity.setExpiresAt(LocalDateTime.now().plusDays(days));
        entity.setLastUsedAt(LocalDateTime.now());
        refreshTokenMapper.insert(entity);

        AuthTokenResp resp = new AuthTokenResp();
        resp.setUserId(userId);
        resp.setAccessToken(accessToken);
        resp.setRefreshToken(refreshToken);
        resp.setExpiresIn(jwtTokenProvider.getAccessTokenExpiresIn());
        return resp;
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((input == null ? "" : input).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 unavailable", ex);
        }
    }
}