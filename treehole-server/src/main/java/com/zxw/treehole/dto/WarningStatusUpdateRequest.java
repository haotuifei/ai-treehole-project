package com.zxw.treehole.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "更新预警处理状态")
public class WarningStatusUpdateRequest {

    @NotBlank
    @Schema(description = "PENDING/PROCESSING/RESOLVED/CLOSED")
    private String status;

    @Size(max = 512)
    private String handleRemark;
}
