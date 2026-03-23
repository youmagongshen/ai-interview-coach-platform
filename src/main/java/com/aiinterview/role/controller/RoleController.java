package com.aiinterview.role.controller;

import com.aiinterview.common.response.ApiResponse;
import com.aiinterview.role.dto.RoleVO;
import com.aiinterview.role.service.RoleService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/inv/roles")
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    public ApiResponse<List<RoleVO>> list() {
        return ApiResponse.ok(roleService.listAll());
    }
}
