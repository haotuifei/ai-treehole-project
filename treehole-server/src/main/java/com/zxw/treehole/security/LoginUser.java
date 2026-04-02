package com.zxw.treehole.security;

import com.zxw.treehole.entity.SysUser;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 当前登录用户（Spring Security UserDetails）
 */
@Getter
public class LoginUser implements UserDetails {

    private final SysUser user;
    private final List<String> roleCodes;

    public LoginUser(SysUser user, List<String> roleCodes) {
        this.user = user;
        this.roleCodes = roleCodes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roleCodes.stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.getStatus() != null && user.getStatus() == 1;
    }
}
