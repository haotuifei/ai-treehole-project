package com.zxw.treehole.common.enums;

import lombok.Getter;

/**
 * 备考目标类型
 */
@Getter
public enum GoalType {
    POSTGRAD("POSTGRAD", "考研"),
    CIVIL_SERVICE("CIVIL_SERVICE", "考公"),
    COURSE("COURSE", "课程"),
    CUSTOM("CUSTOM", "自定义");

    private final String code;
    private final String label;

    GoalType(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static boolean isValid(String code) {
        if (code == null) {
            return false;
        }
        for (GoalType t : values()) {
            if (t.code.equalsIgnoreCase(code)) {
                return true;
            }
        }
        return false;
    }

    public static String normalize(String code) {
        if (code == null) {
            return CUSTOM.code;
        }
        for (GoalType t : values()) {
            if (t.code.equalsIgnoreCase(code)) {
                return t.code;
            }
        }
        return null;
    }
}
