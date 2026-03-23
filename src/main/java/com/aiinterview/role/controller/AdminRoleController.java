package com.aiinterview.role.controller;

import com.aiinterview.common.response.ApiResponse;
import com.aiinterview.role.dto.RoleVO;
import com.aiinterview.role.dto.RoleWeightUpdateReq;
import com.aiinterview.role.dto.RoleWeightUpdateResp;
import com.aiinterview.role.service.RoleService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/inv/admin/roles")
public class AdminRoleController {

    private final RoleService roleService;

    @GetMapping
    public ApiResponse<List<RoleVO>> list() {
        return ApiResponse.ok(roleService.listAll());
    }

    @PutMapping("/{roleId}/weights")
    public ApiResponse<RoleWeightUpdateResp> updateWeights(
            @PathVariable Long roleId,
            @Valid @RequestBody RoleWeightUpdateReq req) {
        return ApiResponse.ok(roleService.updateWeights(roleId, req));
    }
}
