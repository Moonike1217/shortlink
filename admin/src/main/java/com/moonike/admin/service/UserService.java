package com.moonike.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.moonike.admin.dao.entity.UserDO;
import com.moonike.admin.dto.req.UserRegisterReqDTO;
import com.moonike.admin.dto.req.UserUpdateReqDTO;
import com.moonike.admin.dto.resp.UserRespDTO;

/**
 * 用户接口层
 */
public interface UserService extends IService<UserDO> {

    /**
     * 根据用户名查询用户信息
     * @param username 用户名
     * @return 用户返回参数响应
     */
    UserRespDTO getUserByUsername(String username);

    /**
     * 判断用户名是否存在
     * @param username 用户名
     * @return true:存在 false:不存在
     */
    Boolean hasUserName(String username);

    /**
     * 注册用户
     * @param requestParam 用户注册请求参数
     */
    void register(UserRegisterReqDTO requestParam);

    /**
     * 更新用户个人信息
     * @param requestParam
     */
    void update(UserUpdateReqDTO requestParam);
}
