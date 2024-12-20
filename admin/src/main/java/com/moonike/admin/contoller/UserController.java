package com.moonike.admin.contoller;

import com.moonike.admin.common.convention.result.Result;
import com.moonike.admin.common.convention.result.Results;
import com.moonike.admin.dto.req.UserRegisterReqDTO;
import com.moonike.admin.dto.resp.UserRespDTO;
import com.moonike.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制层
 */
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 根据用户名查询用户信息
     */
    @GetMapping("/api/shortlink/v1/user/{username}")
    public Result<UserRespDTO> getUserByUsername (@PathVariable("username") String username) {
        return Results.success(userService.getUserByUsername(username));
    }

    @GetMapping("/api/shortlink/v1/user/has-username")
    public Result<Boolean> hasUserName(@RequestParam("username") String username) {
        return Results.success(userService.hasUserName(username));
    }

    @PostMapping("/api/shortlink/v1/user")
    public Result register(@RequestBody UserRegisterReqDTO requestParam) {
        userService.register(requestParam);
        return Results.success();
    }
}
