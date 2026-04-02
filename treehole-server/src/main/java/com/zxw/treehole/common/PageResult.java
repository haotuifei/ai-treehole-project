package com.zxw.treehole.common;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 统一分页结构
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分页结果")
public class PageResult<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "总条数")
    private long total;
    @Schema(description = "当前页，从 1 开始")
    private long current;
    @Schema(description = "每页条数")
    private long size;
    @Schema(description = "数据列表")
    private List<T> records;

    public static <T> PageResult<T> of(Page<T> page) {
        List<T> rec = page.getRecords() != null ? page.getRecords() : List.of();
        return new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), rec);
    }
}
