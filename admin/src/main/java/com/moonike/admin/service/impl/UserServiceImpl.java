package com.moonike.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.moonike.admin.common.constants.RedisCacheConstant;
import com.moonike.admin.common.convention.exception.ClientException;
import com.moonike.admin.common.enums.UserErrorCodeEnum;
import com.moonike.admin.dao.entity.UserDO;
import com.moonike.admin.dao.mapper.UserMapper;
import com.moonike.admin.dto.req.UserLoginReqDTO;
import com.moonike.admin.dto.req.UserRegisterReqDTO;
import com.moonike.admin.dto.req.UserUpdateReqDTO;
import com.moonike.admin.dto.resp.UserLoginRespDTO;
import com.moonike.admin.dto.resp.UserRespDTO;
import com.moonike.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 用户接口实现层
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    public final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;
    public final RedissonClient redissonClient;
    public final StringRedisTemplate stringRedisTemplate;

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
            throw new ClientException(UserErrorCodeEnum.USER_NULL);
        } else {
            BeanUtils.copyProperties(userDO, result);
            return result;
        }

    }

    @Override
    public Boolean hasUserName(String username) {
//        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
//                .eq(UserDO::getUsername, username);
//        UserDO userDO = baseMapper.selectOne(queryWrapper);
//
//        return userDO == null;

        return userRegisterCachePenetrationBloomFilter.contains(username);
    }

    @Override
    public void register(UserRegisterReqDTO requestParam) {
        if (hasUserName(requestParam.getUsername())) {
            // 尝试使用的用户名已存在，抛出异常
            throw new ClientException(UserErrorCodeEnum.USER_NAME_EXIST);
        }
        // 基于Redisson构造分布式锁
        RLock lock = redissonClient.getLock(RedisCacheConstant.LOCK_USER_REGISTER_KEY + requestParam.getUsername());
        // 开始尝试创建新用户
        try {
            // 尝试获取当前创建用户名的锁
            if (lock.tryLock()) {
                // 尝试创建新用户 将用户信息保存到数据库中
                int inserted = baseMapper.insert(BeanUtil.copyProperties(requestParam, UserDO.class));
                if (inserted < 1) {
                    // 用户信息插入失败
                    throw new ClientException(UserErrorCodeEnum.USER_SAVE_ERROR);
                }
                // 用户创建成功 将用户名添加到布隆过滤器维护的集合中
                userRegisterCachePenetrationBloomFilter.add(requestParam.getUsername());
                // 创建完成 结束调用
            } else {
                // 没拿到锁 说明有多个线程同时注册同一个用户名 直接抛出异常
                throw new ClientException(UserErrorCodeEnum.USER_NAME_EXIST);
            }
        } finally {
            // try-catch-finally 保证释放锁
            lock.unlock();
        }
    }

    @Override
    public void update(UserUpdateReqDTO requestParam) {
        //TODO 判断当前登录用户是否为目标修改用户
        LambdaUpdateWrapper<UserDO> updateWrapper = Wrappers.lambdaUpdate(UserDO.class)
                .eq(UserDO::getUsername, requestParam.getUsername());
        baseMapper.update(BeanUtil.copyProperties(requestParam, UserDO.class), updateWrapper);
    }

    @Override
    public UserLoginRespDTO login(UserLoginReqDTO requestParam) {
        // 尝试从数据库中查询登录用户信息
        LambdaUpdateWrapper<UserDO> queryWrapper = Wrappers.lambdaUpdate(UserDO.class)
                .eq(UserDO::getUsername, requestParam.getUsername())
                .eq(UserDO::getPassword, requestParam.getPassword())
                .eq(UserDO::getDelFlag, 0);
        UserDO user = baseMapper.selectOne(queryWrapper);
        if (user == null) {
            // 查询不到用户 登录信息有误
            throw new ClientException(UserErrorCodeEnum.USER_LOGIN_ERROR);
        }
        //  登录信息校验成功 发放token
        String uuid = UUID.randomUUID().toString();
        stringRedisTemplate.opsForValue().set(RedisCacheConstant.LOCK_USER_LOGIN_KEY + uuid, JSON.toJSONString(user), 30, TimeUnit.MINUTES);
        return new UserLoginRespDTO(uuid);
    }

}
