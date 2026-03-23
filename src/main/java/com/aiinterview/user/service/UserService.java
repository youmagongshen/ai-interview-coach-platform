package com.aiinterview.user.service;

import com.aiinterview.user.dto.UserProfileUpdateReq;
import com.aiinterview.user.dto.UserProfileVO;
import com.aiinterview.user.entity.UserEntity;
import com.baomidou.mybatisplus.extension.service.IService;

public interface UserService extends IService<UserEntity> {

    UserProfileVO getCurrentUser(Long userId);

    UserProfileVO updateCurrentUser(Long userId, UserProfileUpdateReq req);
}
