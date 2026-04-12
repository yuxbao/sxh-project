package com.yizhaoqi.sxh.rag.service;

import com.yizhaoqi.sxh.rag.exception.CustomException;
import com.yizhaoqi.sxh.rag.model.User;
import com.yizhaoqi.sxh.rag.repository.UserRepository;
import com.yizhaoqi.sxh.rag.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SxhBridgeAuthService {

    private static final String SXH_SOURCE = "sxh";
    private static final String SXH_USERNAME_PREFIX = "sxh_";
    private static final Duration BRIDGE_CODE_TTL = Duration.ofMinutes(2);

    private final UserRepository userRepository;
    private final UserService userService;
    private final JwtUtils jwtUtils;
    private final TokenCacheService tokenCacheService;

    @Transactional
    public String createBridgeCode(SxhUserPayload payload) {
        if (payload == null || payload.sxhUserId() == null) {
            throw new CustomException("sxh user id cannot be empty", HttpStatus.BAD_REQUEST);
        }

        User user = ensureSxhUser(payload);
        String bridgeCode = UUID.randomUUID().toString().replace("-", "");

        Map<String, Object> bridgeInfo = new HashMap<>();
        bridgeInfo.put("username", user.getUsername());
        bridgeInfo.put("sxhUserId", String.valueOf(payload.sxhUserId()));
        tokenCacheService.cacheSxhBridgeCode(bridgeCode, bridgeInfo, BRIDGE_CODE_TTL);

        log.info("Created sxh bridge code for sxhUserId={}, username={}", payload.sxhUserId(), user.getUsername());
        return bridgeCode;
    }

    public LoginToken exchangeBridgeCode(String code) {
        if (code == null || code.isBlank()) {
            throw new CustomException("bridge code cannot be empty", HttpStatus.BAD_REQUEST);
        }

        Map<String, Object> bridgeInfo = tokenCacheService.consumeSxhBridgeCode(code);
        if (bridgeInfo == null || bridgeInfo.get("username") == null) {
            throw new CustomException("bridge code is invalid or expired", HttpStatus.UNAUTHORIZED);
        }

        String username = String.valueOf(bridgeInfo.get("username"));
        String token = jwtUtils.generateToken(username);
        String refreshToken = jwtUtils.generateRefreshToken(username);
        return new LoginToken(token, refreshToken);
    }

    public User ensureSxhUser(SxhUserPayload payload) {
        String externalUserId = String.valueOf(payload.sxhUserId());
        String canonicalUsername = SXH_USERNAME_PREFIX + externalUserId;

        Optional<User> existing = userRepository.findByExternalSourceAndExternalUserId(SXH_SOURCE, externalUserId);
        User user = existing.or(() -> userRepository.findByUsername(canonicalUsername))
                .orElseGet(() -> createSxhUser(canonicalUsername));

        boolean changed = false;
        if (!SXH_SOURCE.equals(user.getExternalSource())) {
            user.setExternalSource(SXH_SOURCE);
            changed = true;
        }
        if (!externalUserId.equals(user.getExternalUserId())) {
            user.setExternalUserId(externalUserId);
            changed = true;
        }

        String displayName = resolveDisplayName(payload, canonicalUsername);
        if (!displayName.equals(user.getDisplayName())) {
            user.setDisplayName(displayName);
            changed = true;
        }

        String avatar = payload.avatar();
        if (avatar != null && !avatar.equals(user.getAvatar())) {
            user.setAvatar(avatar);
            changed = true;
        }

        User.Role nextRole = isAdminRole(payload.role()) ? User.Role.ADMIN : User.Role.USER;
        if (user.getRole() != nextRole) {
            user.setRole(nextRole);
            changed = true;
        }

        if (changed) {
            user = userRepository.save(user);
        }
        return user;
    }

    private User createSxhUser(String canonicalUsername) {
        String randomPassword = UUID.randomUUID().toString();
        userService.registerUser(canonicalUsername, randomPassword);
        return userRepository.findByUsername(canonicalUsername)
                .orElseThrow(() -> new CustomException("failed to create sxh bridge user", HttpStatus.INTERNAL_SERVER_ERROR));
    }

    private String resolveDisplayName(SxhUserPayload payload, String fallback) {
        if (payload.displayName() != null && !payload.displayName().isBlank()) {
            return payload.displayName().trim();
        }
        if (payload.userName() != null && !payload.userName().isBlank()) {
            return payload.userName().trim();
        }
        return fallback;
    }

    private boolean isAdminRole(String role) {
        return role != null && "ADMIN".equalsIgnoreCase(role.trim());
    }

    public record SxhUserPayload(
            Long sxhUserId,
            String userName,
            String displayName,
            String avatar,
            String role
    ) {
    }

    public record LoginToken(String token, String refreshToken) {
    }
}
