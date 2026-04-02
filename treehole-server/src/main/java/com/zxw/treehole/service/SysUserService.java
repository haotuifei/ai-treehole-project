package com.zxw.treehole.service;

import com.zxw.treehole.common.PageResult;
import com.zxw.treehole.dto.UserCreateRequest;
import com.zxw.treehole.dto.UserPageQuery;
import com.zxw.treehole.dto.UserUpdateRequest;
import com.zxw.treehole.vo.SysRoleOptionVo;
import com.zxw.treehole.vo.SysUserDetailVo;
import com.zxw.treehole.vo.SysUserPageVo;

import java.util.List;

public interface SysUserService {

    PageResult<SysUserPageVo> pageUsers(UserPageQuery query);

    SysUserDetailVo getDetail(Long id);

    Long createUser(UserCreateRequest request);

    void updateUser(Long id, UserUpdateRequest request);

    /** 逻辑删除 */
    void deleteUser(Long id, Long operatorUserId);

    List<SysRoleOptionVo> listAllRoles();

    /** 供认证模块查询当前用户角色展示 */
    List<String> listRoleCodesByUserId(Long userId);

    List<String> listRoleNamesByUserId(Long userId);
}
