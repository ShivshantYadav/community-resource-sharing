package com.community.dto;

import com.community.entity.Role;

public class UpdateRoleRequest {
    private Role role;

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
