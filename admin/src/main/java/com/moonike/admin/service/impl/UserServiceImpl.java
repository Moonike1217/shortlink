package com.moonike.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.moonike.admin.dao.entity.UserDO;
import com.moonike.admin.dao.mapper.UserMapper;
import com.moonike.admin.dto.resp.UserRespDTO;
import com.moonike.admin.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * 用户接口实现层
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    @Override
    public UserRespDTO getUserByUsername(String username) {
        // 构建查询条件
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, username);
        // 通过 Mybatis-Plus 查询
        UserDO userDO = baseMapper.selectOne(queryWrapper);
        // 将查询结果转换为 DTO
        UserRespDTO result = new UserRespDTO();
        if (userDO == null) {
            return null;
        } else {
            BeanUtils.copyProperties(userDO, result);
            return result;
        }

    }
}
