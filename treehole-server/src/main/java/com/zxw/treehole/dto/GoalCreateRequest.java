package com.zxw.treehole.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "创建备考目标")
public class GoalCreateRequest {

    @NotBlank(message = "目标名称不能为空")
    @Size(max = 128, message = "目标名称最长 128 字")
    private String goalName;

    @NotBlank(message = "目标类型不能为空")
    @Schema(description = "POSTGRAD / CIVIL_SERVICE / COURSE / CUSTOM")
    private String goalType;

    private LocalDate startDate;
    private LocalDate endDate;

    @Size(max = 5000, message = "描述过长")
    private String description;

    @NotNull(message = "状态不能为空")
    @Schema(description = "0进行中 1已完成 2已暂停 3已放弃")
    private Integer status;

    @Size(max = 512, message = "备注最长 512 字")
    private String remark;

    @Schema(description = "排序，默认 0")
    private Integer sortOrder = 0;
}
