package com.zxw.treehole.bootstrap;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zxw.treehole.entity.AlertRule;
import com.zxw.treehole.entity.SysRole;
import com.zxw.treehole.entity.SysUser;
import com.zxw.treehole.entity.SysUserRole;
import com.zxw.treehole.mapper.AlertRuleMapper;
import com.zxw.treehole.mapper.SysRoleMapper;
import com.zxw.treehole.mapper.SysUserMapper;
import com.zxw.treehole.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 首次启动时初始化角色与默认管理员（admin / admin123）
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final SysRoleMapper sysRoleMapper;
    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final AlertRuleMapper alertRuleMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void run(ApplicationArguments args) {
        initRole("ADMIN", "管理员");
        initRole("COUNSELOR", "辅导员");
        initRole("STUDENT", "学生");
        initAdminIfAbsent();
        initDemoUserIfAbsent("counselor", "counselor123", "COUNSELOR", "演示辅导员");
        initDemoUserIfAbsent("student", "student123", "STUDENT", "演示学生");
        linkDemoStudentToCounselor();
        initDefaultAlertRule();
        log.info("数据初始化检查完成（角色 + 演示账号 + 预警规则）");
    }

    private void initRole(String code, String name) {
        Long c = sysRoleMapper.selectCount(new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, code));
        if (c != null && c > 0) {
            return;
        }
        SysRole r = new SysRole();
        r.setRoleCode(code);
        r.setRoleName(name);
        r.setRemark("系统预置");
        sysRoleMapper.insert(r);
        log.info("已创建角色: {}", code);
    }

    private void initAdminIfAbsent() {
        SysUser exist = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, "admin")
                .eq(SysUser::getDeleted, 0));
        if (exist != null) {
            return;
        }
        SysRole adminRole = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, "ADMIN"));
        if (adminRole == null) {
            log.warn("ADMIN 角色不存在，跳过创建默认管理员");
            return;
        }
        SysUser u = new SysUser();
        u.setUsername("admin");
        u.setPassword(passwordEncoder.encode("admin123"));
        u.setRealName("系统管理员");
        u.setStatus(1);
        sysUserMapper.insert(u);
        SysUserRole ur = new SysUserRole();
        ur.setUserId(u.getId());
        ur.setRoleId(adminRole.getId());
        sysUserRoleMapper.insert(ur);
        log.info("已创建默认管理员: admin / admin123 （请在生产环境立即修改密码）");
    }

    /**
     * 演示账号：与 resources/db/data-init-auth-sample.sql 中说明一致
     */
    private void initDemoUserIfAbsent(String username, String rawPassword, String roleCode, String realName) {
        SysUser exist = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)
                .eq(SysUser::getDeleted, 0));
        if (exist != null) {
            return;
        }
        SysRole role = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, roleCode));
        if (role == null) {
            log.warn("角色 {} 不存在，跳过创建用户 {}", roleCode, username);
            return;
        }
        SysUser u = new SysUser();
        u.setUsername(username);
        u.setPassword(passwordEncoder.encode(rawPassword));
        u.setRealName(realName);
        u.setStatus(1);
        sysUserMapper.insert(u);
        SysUserRole ur = new SysUserRole();
        ur.setUserId(u.getId());
        ur.setRoleId(role.getId());
        sysUserRoleMapper.insert(ur);
        log.info("已创建演示用户: {} / {}", username, rawPassword);
    }

    private void linkDemoStudentToCounselor() {
        SysUser st = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, "student")
                .eq(SysUser::getDeleted, 0));
        SysUser co = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, "counselor")
                .eq(SysUser::getDeleted, 0));
        if (st != null && co != null && st.getCounselorId() == null) {
            st.setCounselorId(co.getId());
            sysUserMapper.updateById(st);
            log.info("已将演示学生 student 关联到辅导员 counselor，便于预警联调");
        }
    }

    private void initDefaultAlertRule() {
        Long c = alertRuleMapper.selectCount(new LambdaQueryWrapper<AlertRule>()
                .eq(AlertRule::getRuleCode, "DEFAULT")
                .eq(AlertRule::getDeleted, 0));
        if (c != null && c > 0) {
            return;
        }
        AlertRule r = new AlertRule();
        r.setRuleCode("DEFAULT");
        r.setMediumSentimentThreshold(new BigDecimal("-0.250"));
        r.setHighSentimentThreshold(new BigDecimal("-0.550"));
        r.setKeywordHighEnabled(1);
        r.setRemark("系统默认（可管理端调整）");
        alertRuleMapper.insert(r);
        log.info("已创建默认预警规则 DEFAULT");
    }
}
