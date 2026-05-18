package com.zxw.treehole.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "辅导员班级分配请求")
public class CounselorClassRequest {

    @NotNull
    @Schema(description = "辅导员用户ID")
    private Long counselorUserId;

    @NotBlank
    @Schema(description = "班级名称")
    private String className;
}
