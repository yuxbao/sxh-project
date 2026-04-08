package com.yizhaoqi.smartpai.service;

import com.yizhaoqi.smartpai.exception.CustomException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class SxhSharedUserAuthService {

    private static final String INVALID_SHARED_LOGIN_MESSAGE = "思享汇账号或密码错误，请使用社区账号密码登录";

    private static final String QUERY_BY_LOGIN_NAME = """
            select u.id as user_id,
                   u.user_name as login_name,
                   u.password as password_hash
            from user u
            where u.deleted = 0 and u.user_name = ?
            limit 1
            """;

    private final JdbcTemplate jdbcTemplate;

    public SxhSharedUserAuthService(@Qualifier("sxhUserJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Value("${sxh.user.security.salt:tech_π}")
    private String salt;

    @Value("${sxh.user.security.salt-index:3}")
    private Integer saltIndex;

    private final RowMapper<SharedUser> userRowMapper = (rs, rowNum) -> new SharedUser(
            rs.getLong("user_id"),
            rs.getString("login_name"),
            rs.getString("login_name"),
            null,
            "USER",
            rs.getString("password_hash")
    );

    public SharedUser authenticate(String loginName, String password) {
        SharedUser user = findByLoginName(loginName);
        if (!match(password, user.passwordHash())) {
            throw new CustomException(INVALID_SHARED_LOGIN_MESSAGE, HttpStatus.UNAUTHORIZED);
        }
        return user;
    }

    public SharedUser findByLoginName(String loginName) {
        List<SharedUser> users = jdbcTemplate.query(QUERY_BY_LOGIN_NAME, userRowMapper, loginName);
        if (users.isEmpty()) {
            throw new CustomException(INVALID_SHARED_LOGIN_MESSAGE, HttpStatus.UNAUTHORIZED);
        }
        return users.get(0);
    }

    private boolean match(String plainPwd, String encodedPwd) {
        return encodePassword(plainPwd).equals(encodedPwd);
    }

    private String encodePassword(String plainPwd) {
        if (plainPwd.length() > saltIndex) {
            plainPwd = plainPwd.substring(0, saltIndex) + salt + plainPwd.substring(saltIndex);
        } else {
            plainPwd = plainPwd + salt;
        }
        return DigestUtils.md5DigestAsHex(plainPwd.getBytes(StandardCharsets.UTF_8));
    }

    public record SharedUser(
            Long userId,
            String loginName,
            String displayName,
            String avatar,
            String role,
            String passwordHash
    ) {
    }
}
