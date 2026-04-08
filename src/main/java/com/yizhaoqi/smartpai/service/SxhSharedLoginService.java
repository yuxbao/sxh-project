package com.yizhaoqi.smartpai.service;

import com.yizhaoqi.smartpai.model.User;
import com.yizhaoqi.smartpai.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SxhSharedLoginService {

    private final SxhSharedUserAuthService sxhSharedUserAuthService;
    private final SxhBridgeAuthService sxhBridgeAuthService;
    private final JwtUtils jwtUtils;

    @Transactional
    public SxhBridgeAuthService.LoginToken login(String loginName, String password) {
        SxhSharedUserAuthService.SharedUser sharedUser = sxhSharedUserAuthService.authenticate(loginName, password);

        User localUser = sxhBridgeAuthService.ensureSxhUser(new SxhBridgeAuthService.SxhUserPayload(
                sharedUser.userId(),
                sharedUser.loginName(),
                sharedUser.displayName(),
                sharedUser.avatar(),
                sharedUser.role()
        ));

        String token = jwtUtils.generateToken(localUser.getUsername());
        String refreshToken = jwtUtils.generateRefreshToken(localUser.getUsername());
        return new SxhBridgeAuthService.LoginToken(token, refreshToken);
    }
}
