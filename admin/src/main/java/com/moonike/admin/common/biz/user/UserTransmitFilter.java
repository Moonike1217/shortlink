package com.moonike.admin.common.biz.user;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.google.common.collect.Lists;
import com.moonike.admin.common.constants.RedisCacheConstant;
import com.moonike.admin.common.convention.exception.ClientException;
import com.moonike.admin.common.enums.UserErrorCodeEnum;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.IOException;
import java.util.List;

/**
 * 用户信息传输过滤器
 */
@RequiredArgsConstructor
public class UserTransmitFilter implements Filter {

    private final StringRedisTemplate stringRedisTemplate;
    private final List<String> IGNORE_URI = Lists.newArrayList(
            "/api/shortlink/admin/v1/user/check-login",
            "/api/shortlink/admin/v1/user/login",
            "/api/shortlink/admin/v1/user/has-username"
    );
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        // 将ServletRequest强制转换为HttpServletRequest，以获取更多HTTP请求相关的方法
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        // 从HTTP请求头中获取用户名
        String username = httpServletRequest.getHeader("username");
        // 从HTTP请求头中获取令牌
        String token = httpServletRequest.getHeader("token");
        // 获取请求的URI，用于判断请求的目的地
        String requestURI = httpServletRequest.getRequestURI();
        // 检查请求URI是否为目标URI，以决定是否需要验证用户登录状态
        if (!IGNORE_URI.contains(requestURI)) {
            // 目标URI为非登录URI 需要验证身份
            if(!(httpServletRequest.getMethod().equals("POST") && requestURI.equals("/api/shortlink/admin/v1/user"))) {
                // 如果为POST请求，并且请求URI为/api/shortlink/admin/v1/user，则证明为注册请求，放行
                // 如果用户名或令牌为空，则抛出异常
                if (!StrUtil.isAllNotBlank(username, token)) {
                    throw new ClientException(UserErrorCodeEnum.USER_TOKEN_ERROR);
                }
                // 从Redis中获取用户信息，键由用户名和令牌组成，用于验证用户登录状态
                Object userInfoJsonStr = null;
                try {
                    userInfoJsonStr = stringRedisTemplate.opsForHash().get(RedisCacheConstant.LOCK_USER_LOGIN_KEY + username, token);
                    if (userInfoJsonStr == null) {
                        throw new ClientException(UserErrorCodeEnum.USER_TOKEN_ERROR);
                    }
                } catch (Exception e) {
                    throw new ClientException(UserErrorCodeEnum.USER_TOKEN_ERROR);
                }
                // 如果获取到用户信息，则解析并设置用户上下文
                if (userInfoJsonStr != null) {
                    UserInfoDTO userInfoDTO = JSON.parseObject(userInfoJsonStr.toString(), UserInfoDTO.class);
                    UserContext.setUser(userInfoDTO);
                }
            }
        }
        // 尝试执行过滤链，无论执行结果如何，最终都会移除用户上下文
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            // 移除用户上下文，确保每个请求之间用户信息不互相干扰
            UserContext.removeUser();
        }
    }
}