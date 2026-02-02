package com.github.paicoding.forum.web.websocket;

import com.github.paicoding.forum.core.util.SessionUtil;
import com.github.paicoding.forum.service.user.service.LoginService;
import com.github.paicoding.forum.service.user.service.UserService;
import com.github.paicoding.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

@Component
public class SessionPrincipalHandshakeHandler extends DefaultHandshakeHandler {
    private final UserService userService;

    public SessionPrincipalHandshakeHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected Principal determineUser(ServerHttpRequest request,
                                      WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String session = SessionUtil.findCookieByName(request, LoginService.SESSION_KEY);
        if (StringUtils.isBlank(session)) {
            session = request.getHeaders().getFirst("Authorization");
        }

        if (StringUtils.isNotBlank(session)) {
            BaseUserInfoDTO user = userService.getAndUpdateUserIpInfoBySessionId(session, null);
            if (user != null && user.getUserId() != null) {
                return new WebSocketUserPrincipal(String.valueOf(user.getUserId()));
            }
        }

        return new WebSocketUserPrincipal("anon-" + UUID.randomUUID());
    }
}
