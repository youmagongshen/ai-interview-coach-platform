package com.aiinterview.user.dto;

import lombok.Data;

@Data
public class UserProfileVO {

    private Long id;
    private String username;
    private String phone;
    private String email;
    private Integer status;
}
