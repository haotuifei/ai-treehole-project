package com.zxw.treehole.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "更新备考目标（仅传需要修改的字段）")
public class GoalUpdateRequest {

    @Size(max = 128, message = "目标名称最长 128 字")
    private String goalName;

    @Schema(description = "POSTGRAD / CIVIL_SERVICE / COURSE / CUSTOM")
    private String goalType;

    private LocalDate startDate;
    private LocalDate endDate;

    @Size(max = 5000, message = "描述过长")
    private String description;

    @Schema(description = "0进行中 1已完成 2已暂停 3已放弃")
    private Integer status;

    @Size(max = 512, message = "备注最长 512 字")
    private String remark;

    private Integer sortOrder;
}
