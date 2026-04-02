package com.zxw.treehole.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "更新学习打卡")
public class CheckinUpdateRequest {

    private LocalDate checkDate;

    @Min(value = 0, message = "学习时长不能为负")
    @Max(value = 24 * 60, message = "单日时长不超过 1440 分钟")
    private Integer durationMinutes;

    @Size(max = 1024, message = "打卡内容过长")
    private String content;

    @Min(0)
    @Max(1)
    @Schema(description = "0 否 1 是")
    private Integer pomodoroDone;

    @Size(max = 64, message = "心情描述过长")
    private String mood;

    @Size(max = 512, message = "备注过长")
    private String remark;
}
