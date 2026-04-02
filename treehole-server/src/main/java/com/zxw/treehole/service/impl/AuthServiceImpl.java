package com.zxw.treehole.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zxw.treehole.dto.LoginRequest;
import com.zxw.treehole.dto.RegisterRequest;
import com.zxw.treehole.entity.SysRole;
import com.zxw.treehole.entity.SysUser;
import com.zxw.treehole.entity.SysUserRole;
import com.zxw.treehole.exception.BusinessException;
import com.zxw.treehole.mapper.SysRoleMapper;
import com.zxw.treehole.mapper.SysUserMapper;
import com.zxw.treehole.mapper.SysUserRoleMapper;
import com.zxw.treehole.security.JwtProperties;
import com.zxw.treehole.security.JwtUtil;
import com.zxw.treehole.security.LoginUser;
import com.zxw.treehole.service.AuthService;
import com.zxw.treehole.service.SysUserService;
import com.zxw.treehole.vo.LoginVo;
import com.zxw.treehole.vo.UserBriefVo;
import com.zxw.treehole.vo.UserProfileVo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;
    private final PasswordEncoder passwordEncoder;
    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysUserService sysUserService;

    @Override
    public LoginVo login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        LoginUser loginUser = (LoginUser) auth.getPrincipal();
        SysUser u = loginUser.getUser();
        List<String> roles = loginUser.getRoleCodes();
        String token = jwtUtil.createToken(u.getId(), u.getUsername(), roles);
        LoginVo vo = new LoginVo();
        vo.setToken(token);
        vo.setTokenType("Bearer");
        vo.setExpiresIn(jwtProperties.getExpirationMs());
        vo.setUser(toBrief(u));
        vo.setRoles(roles);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterRequest request) {
        Long cnt = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.getUsername())
                .eq(SysUser::getDeleted, 0));
        if (cnt != null && cnt > 0) {
            throw new BusinessException("用户名已存在");
        }
        SysRole studentRole = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, "STUDENT"));
        if (studentRole == null) {
            throw new BusinessException("系统未初始化角色，请联系管理员执行初始化脚本");
        }
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setStudentNo(request.getStudentNo());
        user.setClassName(request.getClassName());
        user.setStatus(1);
        sysUserMapper.insert(user);
        SysUserRole ur = new SysUserRole();
        ur.setUserId(user.getId());
        ur.setRoleId(studentRole.getId());
        sysUserRoleMapper.insert(ur);
    }

    @Override
    public UserProfileVo currentProfile(Long userId) {
        SysUser u = sysUserMapper.selectById(userId);
        if (u == null || (u.getDeleted() != null && u.getDeleted() == 1)) {
            throw new BusinessException("用户不存在");
        }
        UserProfileVo vo = new UserProfileVo();
        vo.setId(u.getId());
        vo.setUsername(u.getUsername());
        vo.setRealName(u.getRealName());
        vo.setPhone(u.getPhone());
        vo.setEmail(u.getEmail());
        vo.setStudentNo(u.getStudentNo());
        vo.setClassName(u.getClassName());
        vo.setCounselorId(u.getCounselorId());
        vo.setAvatarUrl(u.getAvatarUrl());
        vo.setStatus(u.getStatus());
        vo.setRoleCodes(sysUserService.listRoleCodesByUserId(userId));
        vo.setRoleNames(sysUserService.listRoleNamesByUserId(userId));
        return vo;
    }

    private static UserBriefVo toBrief(SysUser u) {
        UserBriefVo v = new UserBriefVo();
        v.setId(u.getId());
        v.setUsername(u.getUsername());
        v.setRealName(u.getRealName());
        v.setStudentNo(u.getStudentNo());
        v.setClassName(u.getClassName());
        v.setAvatarUrl(u.getAvatarUrl());
        return v;
    }
}
