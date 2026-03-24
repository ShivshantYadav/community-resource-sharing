package com.community.entity;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Role {
    OWNER,
    USER,
    ADMIN;

    @JsonCreator
    public static Role from(String value) {
        if (value == null) return null;
        return Role.valueOf(value.toUpperCase()); // accepts any case
    }
}
