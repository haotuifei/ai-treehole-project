package com.zxw.treehole.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "辅导员端-干预记录列表行")
public class InterventionListItemVo {

    private Long id;
    private Long warningRecordId;

    @Schema(description = "预警风险等级")
    private String warningRiskLevel;

    @Schema(description = "预警触发内容摘要")
    private String warningTextSummary;

    private Long studentUserId;
    private String studentName;
    private String studentUsername;
    private String className;

    private Long counselorUserId;
    private String counselorName;

    private String content;
    private LocalDateTime interventionTime;
}
