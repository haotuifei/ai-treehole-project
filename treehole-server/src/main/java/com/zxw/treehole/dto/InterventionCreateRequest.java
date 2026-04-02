package com.zxw.treehole.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "新增干预记录")
public class InterventionCreateRequest {

    @NotBlank
    @Size(max = 2000)
    private String content;
}
