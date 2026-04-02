package com.zxw.treehole.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "预警列表行")
public class WarningListItemVo {

    private Long id;
    private Long userId;
    private String studentName;
    private String studentUsername;
    private String className;
    private Long sessionId;
    private String textSummary;
    private String triggerSource;
    private String riskLevel;
    private String status;
    private LocalDateTime createTime;
}
