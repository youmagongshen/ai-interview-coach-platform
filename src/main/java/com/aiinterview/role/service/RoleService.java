package com.aiinterview.role.service;

import com.aiinterview.role.dto.RoleVO;
import com.aiinterview.role.dto.RoleWeightUpdateReq;
import com.aiinterview.role.dto.RoleWeightUpdateResp;
import com.aiinterview.role.entity.RoleEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

public interface RoleService extends IService<RoleEntity> {

    List<RoleVO> listAll();

    RoleWeightUpdateResp updateWeights(Long roleId, RoleWeightUpdateReq req);
}
