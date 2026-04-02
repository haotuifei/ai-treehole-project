package com.zxw.treehole.service;

import com.zxw.treehole.dto.LoginRequest;
import com.zxw.treehole.dto.RegisterRequest;
import com.zxw.treehole.vo.LoginVo;
import com.zxw.treehole.vo.UserBriefVo;
import com.zxw.treehole.vo.UserProfileVo;

public interface AuthService {

    LoginVo login(LoginRequest request);

    void register(RegisterRequest request);

    UserProfileVo currentProfile(Long userId);
}
