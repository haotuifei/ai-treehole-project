package com.zxw.treehole.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InterventionVo {

    private Long id;
    private Long counselorUserId;
    private String counselorName;
    private String content;
    private LocalDateTime interventionTime;
}
