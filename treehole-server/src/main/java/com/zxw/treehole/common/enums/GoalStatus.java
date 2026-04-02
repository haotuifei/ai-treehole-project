package com.zxw.treehole.common.enums;

import lombok.Getter;

/**
 * 备考目标状态
 */
@Getter
public enum GoalStatus {
    IN_PROGRESS(0, "进行中"),
    DONE(1, "已完成"),
    PAUSED(2, "已暂停"),
    ABANDONED(3, "已放弃");

    private final int code;
    private final String label;

    GoalStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static boolean isValid(Integer status) {
        if (status == null) {
            return false;
        }
        for (GoalStatus s : values()) {
            if (s.code == status) {
                return true;
            }
        }
        return false;
    }
}
