package com.zxw.treehole.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "创建学习打卡")
public class CheckinCreateRequest {

    @NotNull(message = "所属目标不能为空")
    private Long goalId;

    @NotNull(message = "打卡日期不能为空")
    private LocalDate checkDate;

    @Min(value = 0, message = "学习时长不能为负")
    @Max(value = 24 * 60, message = "单日时长不超过 1440 分钟")
    private Integer durationMinutes;

    @Size(max = 1024, message = "打卡内容过长")
    private String content;

    @NotNull(message = "请填写是否完成番茄钟")
    @Schema(description = "0 否 1 是")
    @Min(0)
    @Max(1)
    private Integer pomodoroDone;

    @Size(max = 64, message = "心情描述过长")
    @Schema(description = "今日心情，如：不错 / 一般 / 疲惫")
    private String mood;

    @Size(max = 512, message = "备注过长")
    private String remark;
}
