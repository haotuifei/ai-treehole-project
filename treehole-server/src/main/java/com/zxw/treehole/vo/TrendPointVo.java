package com.zxw.treehole.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "单日学习时长点")
public class TrendPointVo {

    private LocalDate date;
    /** 当日学习总分钟数（多笔打卡求和） */
    private Long minutes;
}
