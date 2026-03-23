package com.aiinterview.auth.service;

import com.aiinterview.auth.dto.AuthTokenResp;
import com.aiinterview.auth.dto.LoginReq;
import com.aiinterview.auth.dto.RegisterReq;
import com.aiinterview.auth.dto.RegisterResp;

public interface AuthService {

    RegisterResp register(RegisterReq req);

    AuthTokenResp login(LoginReq req);

    AuthTokenResp refresh(String refreshToken);

    boolean logout(String refreshToken);
}
