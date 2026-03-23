package com.aiinterview.role.service.impl;

import com.aiinterview.role.dto.RoleVO;
import com.aiinterview.role.dto.RoleWeightUpdateReq;
import com.aiinterview.role.dto.RoleWeightUpdateResp;
import com.aiinterview.role.entity.RoleEntity;
import com.aiinterview.role.mapper.RoleMapper;
import com.aiinterview.role.service.RoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, RoleEntity> implements RoleService {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    @Override
    public List<RoleVO> listAll() {
        return baseMapper.selectAll()
                .stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public RoleWeightUpdateResp updateWeights(Long roleId, RoleWeightUpdateReq req) {
        BigDecimal total = req.getWeightCorrectness()
                .add(req.getWeightDepth())
                .add(req.getWeightLogic())
                .add(req.getWeightMatch())
                .add(req.getWeightExpression());

        if (total.compareTo(HUNDRED) != 0) {
            return new RoleWeightUpdateResp(false, total);
        }

        RoleEntity exists = baseMapper.selectById(roleId);
        if (exists == null) {
            return new RoleWeightUpdateResp(false, total);
        }

        RoleEntity update = new RoleEntity();
        update.setId(roleId);
        update.setDescription(req.getDescription());
        update.setWeightCorrectness(req.getWeightCorrectness());
        update.setWeightDepth(req.getWeightDepth());
        update.setWeightLogic(req.getWeightLogic());
        update.setWeightMatch(req.getWeightMatch());
        update.setWeightExpression(req.getWeightExpression());
        boolean updated = baseMapper.updateById(update) > 0;
        return new RoleWeightUpdateResp(updated, total);
    }

    private RoleVO toVO(RoleEntity entity) {
        RoleVO vo = new RoleVO();
        vo.setId(entity.getId());
        vo.setCode(entity.getCode());
        vo.setName(entity.getName());
        vo.setDescription(entity.getDescription());
        vo.setWeightCorrectness(entity.getWeightCorrectness());
        vo.setWeightDepth(entity.getWeightDepth());
        vo.setWeightLogic(entity.getWeightLogic());
        vo.setWeightMatch(entity.getWeightMatch());
        vo.setWeightExpression(entity.getWeightExpression());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }
}
