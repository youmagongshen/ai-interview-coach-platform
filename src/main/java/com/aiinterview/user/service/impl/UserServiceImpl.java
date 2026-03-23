package com.aiinterview.user.service.impl;

import com.aiinterview.user.dto.UserProfileUpdateReq;
import com.aiinterview.user.dto.UserProfileVO;
import com.aiinterview.user.entity.UserEntity;
import com.aiinterview.user.mapper.UserMapper;
import com.aiinterview.user.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {

    @Override
    public UserProfileVO getCurrentUser(Long userId) {
        UserEntity entity = baseMapper.selectById(userId);
        return toVO(entity);
    }

    @Override
    public UserProfileVO updateCurrentUser(Long userId, UserProfileUpdateReq req) {
        UserEntity update = new UserEntity();
        update.setId(userId);
        update.setPhone(req.getPhone());
        update.setEmail(req.getEmail());
        baseMapper.updateProfile(update);
        return getCurrentUser(userId);
    }

    private UserProfileVO toVO(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        UserProfileVO vo = new UserProfileVO();
        vo.setId(entity.getId());
        vo.setUsername(entity.getUsername());
        vo.setPhone(entity.getPhone());
        vo.setEmail(entity.getEmail());
        vo.setStatus(entity.getStatus());
        return vo;
    }
}
