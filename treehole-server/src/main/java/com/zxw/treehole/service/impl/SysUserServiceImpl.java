package com.zxw.treehole.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zxw.treehole.common.PageResult;
import com.zxw.treehole.dto.UserCreateRequest;
import com.zxw.treehole.dto.UserPageQuery;
import com.zxw.treehole.dto.UserUpdateRequest;
import com.zxw.treehole.entity.SysRole;
import com.zxw.treehole.entity.SysUser;
import com.zxw.treehole.entity.SysUserRole;
import com.zxw.treehole.exception.BusinessException;
import com.zxw.treehole.mapper.SysRoleMapper;
import com.zxw.treehole.mapper.SysUserMapper;
import com.zxw.treehole.mapper.SysUserRoleMapper;
import com.zxw.treehole.service.SysUserService;
import com.zxw.treehole.vo.SysRoleOptionVo;
import com.zxw.treehole.vo.SysUserDetailVo;
import com.zxw.treehole.vo.SysUserPageVo;
import com.zxw.treehole.vo.UserRoleVo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl implements SysUserService {

    private static final String ROLE_ADMIN = "ADMIN";

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public PageResult<SysUserPageVo> pageUsers(UserPageQuery query) {
        Page<SysUser> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<SysUser> w = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getDeleted, 0)
                .orderByDesc(SysUser::getCreateTime);
        if (StringUtils.hasText(query.getKeyword())) {
            String kw = query.getKeyword().trim();
            w.and(x -> x.like(SysUser::getUsername, kw).or().like(SysUser::getRealName, kw));
        }
        if (query.getStatus() != null) {
            w.eq(SysUser::getStatus, query.getStatus());
        }
        if (StringUtils.hasText(query.getRoleCode())) {
            SysRole role = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                    .eq(SysRole::getRoleCode, query.getRoleCode().trim()));
            if (role == null) {
                return emptyPage(query);
            }
            List<Long> userIds = sysUserRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                            .eq(SysUserRole::getRoleId, role.getId()))
                    .stream()
                    .map(SysUserRole::getUserId)
                    .distinct()
                    .toList();
            if (userIds.isEmpty()) {
                return emptyPage(query);
            }
            w.in(SysUser::getId, userIds);
        }
        Page<SysUser> result = sysUserMapper.selectPage(page, w);
        List<SysUserPageVo> rows = toPageVos(result.getRecords());
        Page<SysUserPageVo> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(rows);
        return PageResult.of(voPage);
    }

    @Override
    public SysUserDetailVo getDetail(Long id) {
        SysUser user = requireUser(id);
        SysUserDetailVo vo = toDetailVo(user);
        vo.setRoles(loadUserRoleVos(id));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createUser(UserCreateRequest request) {
        Long cnt = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.getUsername())
                .eq(SysUser::getDeleted, 0));
        if (cnt != null && cnt > 0) {
            throw new BusinessException("用户名已存在");
        }
        validateRoleIds(request.getRoleIds());
        SysUser u = new SysUser();
        u.setUsername(request.getUsername());
        u.setPassword(passwordEncoder.encode(request.getPassword()));
        u.setRealName(request.getRealName());
        u.setPhone(request.getPhone());
        u.setEmail(request.getEmail());
        u.setStudentNo(request.getStudentNo());
        u.setClassName(request.getClassName());
        u.setCounselorId(request.getCounselorId());
        u.setAvatarUrl(request.getAvatarUrl());
        u.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        sysUserMapper.insert(u);
        replaceUserRoles(u.getId(), request.getRoleIds());
        return u.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(Long id, UserUpdateRequest request) {
        SysUser user = requireUser(id);
        if (request.getRealName() != null) {
            user.setRealName(request.getRealName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getStudentNo() != null) {
            user.setStudentNo(request.getStudentNo());
        }
        if (request.getClassName() != null) {
            user.setClassName(request.getClassName());
        }
        if (request.getCounselorId() != null) {
            user.setCounselorId(request.getCounselorId());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        if (StringUtils.hasText(request.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        sysUserMapper.updateById(user);
        if (request.getRoleIds() != null) {
            if (request.getRoleIds().isEmpty()) {
                throw new BusinessException("至少保留一个角色");
            }
            validateRoleIds(request.getRoleIds());
            assertNotRemovingLastAdmin(id, request.getRoleIds());
            replaceUserRoles(id, request.getRoleIds());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id, Long operatorUserId) {
        if (Objects.equals(id, operatorUserId)) {
            throw new BusinessException("不能删除当前登录账号");
        }
        SysUser user = requireUser(id);
        if (userHasAdminRole(id) && countAdminUsers() <= 1) {
            throw new BusinessException("不能删除系统最后一个管理员账号");
        }
        sysUserMapper.deleteById(id);
    }

    @Override
    public List<SysRoleOptionVo> listAllRoles() {
        return sysRoleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                        .orderByAsc(SysRole::getId))
                .stream()
                .map(r -> {
                    SysRoleOptionVo v = new SysRoleOptionVo();
                    v.setId(r.getId());
                    v.setRoleCode(r.getRoleCode());
                    v.setRoleName(r.getRoleName());
                    return v;
                })
                .toList();
    }

    @Override
    public List<String> listRoleCodesByUserId(Long userId) {
        return loadUserRoleVos(userId).stream().map(UserRoleVo::getRoleCode).toList();
    }

    @Override
    public List<String> listRoleNamesByUserId(Long userId) {
        return loadUserRoleVos(userId).stream().map(UserRoleVo::getRoleName).toList();
    }

    private SysUser requireUser(Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null || (user.getDeleted() != null && user.getDeleted() == 1)) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    private List<SysUserPageVo> toPageVos(List<SysUser> users) {
        if (users.isEmpty()) {
            return List.of();
        }
        Map<Long, List<String>> roleMap = batchRoleCodes(users.stream().map(SysUser::getId).toList());
        List<SysUserPageVo> list = new ArrayList<>();
        for (SysUser u : users) {
            SysUserPageVo vo = new SysUserPageVo();
            copyBase(u, vo);
            vo.setRoleCodes(roleMap.getOrDefault(u.getId(), List.of()));
            list.add(vo);
        }
        return list;
    }

    private static void copyBase(SysUser u, SysUserPageVo vo) {
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
        vo.setCreateTime(u.getCreateTime());
        vo.setUpdateTime(u.getUpdateTime());
    }

    private SysUserDetailVo toDetailVo(SysUser u) {
        SysUserDetailVo vo = new SysUserDetailVo();
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
        vo.setCreateTime(u.getCreateTime());
        vo.setUpdateTime(u.getUpdateTime());
        return vo;
    }

    private Map<Long, List<String>> batchRoleCodes(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        List<SysUserRole> urs = sysUserRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                .in(SysUserRole::getUserId, userIds));
        if (urs.isEmpty()) {
            return userIds.stream().collect(Collectors.toMap(uid -> uid, uid -> new ArrayList<>()));
        }
        Set<Long> roleIds = urs.stream().map(SysUserRole::getRoleId).collect(Collectors.toSet());
        Map<Long, SysRole> roleById = sysRoleMapper.selectList(new LambdaQueryWrapper<SysRole>().in(SysRole::getId, roleIds))
                .stream()
                .collect(Collectors.toMap(SysRole::getId, r -> r));
        Map<Long, List<String>> map = new HashMap<>();
        for (Long uid : userIds) {
            map.put(uid, new ArrayList<>());
        }
        for (SysUserRole ur : urs) {
            SysRole r = roleById.get(ur.getRoleId());
            if (r != null) {
                map.computeIfAbsent(ur.getUserId(), k -> new ArrayList<>()).add(r.getRoleCode());
            }
        }
        return map;
    }

    private List<UserRoleVo> loadUserRoleVos(Long userId) {
        List<SysUserRole> urs = sysUserRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId));
        if (urs.isEmpty()) {
            return List.of();
        }
        Set<Long> roleIds = urs.stream().map(SysUserRole::getRoleId).collect(Collectors.toSet());
        Map<Long, SysRole> roleById = sysRoleMapper.selectList(new LambdaQueryWrapper<SysRole>().in(SysRole::getId, roleIds))
                .stream()
                .collect(Collectors.toMap(SysRole::getId, r -> r));
        List<UserRoleVo> list = new ArrayList<>();
        for (SysUserRole ur : urs) {
            SysRole r = roleById.get(ur.getRoleId());
            if (r == null) {
                continue;
            }
            UserRoleVo v = new UserRoleVo();
            v.setRoleId(r.getId());
            v.setRoleCode(r.getRoleCode());
            v.setRoleName(r.getRoleName());
            list.add(v);
        }
        return list;
    }

    private void validateRoleIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            throw new BusinessException("至少选择一个角色");
        }
        Set<Long> unique = new HashSet<>(roleIds);
        Long cnt = sysRoleMapper.selectCount(new LambdaQueryWrapper<SysRole>().in(SysRole::getId, unique));
        if (cnt == null || cnt != unique.size()) {
            throw new BusinessException("存在无效的角色 ID");
        }
    }

    private void replaceUserRoles(Long userId, List<Long> roleIds) {
        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        for (Long rid : roleIds) {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(userId);
            ur.setRoleId(rid);
            sysUserRoleMapper.insert(ur);
        }
    }

    private boolean userHasAdminRole(Long userId) {
        SysRole admin = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, ROLE_ADMIN));
        if (admin == null) {
            return false;
        }
        Long c = sysUserRoleMapper.selectCount(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId)
                .eq(SysUserRole::getRoleId, admin.getId()));
        return c != null && c > 0;
    }

    private long countAdminUsers() {
        SysRole admin = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, ROLE_ADMIN));
        if (admin == null) {
            return 0;
        }
        List<SysUserRole> urs = sysUserRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getRoleId, admin.getId()));
        Set<Long> ids = new HashSet<>();
        for (SysUserRole ur : urs) {
            SysUser u = sysUserMapper.selectById(ur.getUserId());
            if (u != null && (u.getDeleted() == null || u.getDeleted() == 0)) {
                ids.add(u.getId());
            }
        }
        return ids.size();
    }

    private void assertNotRemovingLastAdmin(Long userId, List<Long> newRoleIds) {
        if (!userHasAdminRole(userId)) {
            return;
        }
        SysRole admin = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, ROLE_ADMIN));
        if (admin == null) {
            return;
        }
        boolean stillAdmin = newRoleIds.contains(admin.getId());
        if (!stillAdmin && countAdminUsers() <= 1) {
            throw new BusinessException("不能移除系统最后一个管理员角色");
        }
    }

    private static PageResult<SysUserPageVo> emptyPage(UserPageQuery query) {
        Page<SysUserPageVo> p = new Page<>(query.getPageNum(), query.getPageSize(), 0);
        p.setRecords(List.of());
        return PageResult.of(p);
    }
}
