package com.zxw.treehole.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class WarningDetailVo {

    private Long id;
    private Long userId;
    private String studentName;
    private String studentUsername;
    private String className;
    private Long sessionId;
    private Long emotionRecordId;
    private String textSummary;
    private String triggerSource;
    private String riskLevel;
    private String status;
    private Long handlerUserId;
    private String handleRemark;
    private LocalDateTime handledAt;
    private LocalDateTime createTime;

    private EmotionRecordBriefVo emotion;
    private List<InterventionVo> interventions;
}
