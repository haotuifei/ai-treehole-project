package com.zxw.treehole.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "辅导员端-学生列表行")
public class StudentProfileVo {

    private Long id;
    private String username;
    private String realName;
    private String studentNo;
    private String className;
    private String phone;
    private String email;
    private Integer status;
    private LocalDateTime createTime;

    @Schema(description = "预警总数")
    private Long warningCount;

    @Schema(description = "情绪记录总数")
    private Long emotionCount;

    @Schema(description = "最近情绪分值")
    private BigDecimal lastEmotionScore;

    @Schema(description = "最近情绪记录时间")
    private LocalDateTime lastEmotionTime;
}
