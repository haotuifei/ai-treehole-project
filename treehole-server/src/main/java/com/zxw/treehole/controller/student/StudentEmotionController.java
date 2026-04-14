package com.zxw.treehole.controller.student;

import com.zxw.treehole.common.PageResult;
import com.zxw.treehole.common.Result;
import com.zxw.treehole.dto.EmotionStatsQuery;
import com.zxw.treehole.entity.EmotionRecord;
import com.zxw.treehole.mapper.EmotionRecordMapper;
import com.zxw.treehole.security.LoginUser;
import com.zxw.treehole.service.EmotionStatsService;
import com.zxw.treehole.vo.EmotionRecordBriefVo;
import com.zxw.treehole.vo.EmotionStatsVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static com.zxw.treehole.config.OpenApiConfig.SECURITY_SCHEME_NAME;

@Tag(name = "学生端-情绪统计", description = "情绪数据统计与历史记录")
@RestController
@RequestMapping("/api/student/emotion")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
@SecurityRequirement(name = SECURITY_SCHEME_NAME)
public class StudentEmotionController {

    private final EmotionStatsService emotionStatsService;
    private final EmotionRecordMapper emotionRecordMapper;

    @Operation(summary = "获取情绪统计数据")
    @GetMapping("/stats")
    public Result<EmotionStatsVo> getStats(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestParam String period) {
        EmotionStatsQuery query = new EmotionStatsQuery();
        query.setPeriod(period);
        return Result.ok(emotionStatsService.getStats(loginUser.getUser().getId(), query));
    }

    @Operation(summary = "获取情绪记录列表（分页）")
    @GetMapping("/records")
    public Result<PageResult<EmotionRecordBriefVo>> listRecords(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestParam(required = false, defaultValue = "1") int pageNum,
            @RequestParam(required = false, defaultValue = "20") int pageSize,
            @RequestParam(required = false) String period) {

        LambdaQueryWrapper<EmotionRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EmotionRecord::getUserId, loginUser.getUser().getId())
                .eq(EmotionRecord::getDeleted, 0)
                .orderByDesc(EmotionRecord::getCreateTime);

        if (period != null) {
            LocalDate today = LocalDate.now();
            LocalDateTime startTime;
            switch (period) {
                case "day":
                    startTime = today.atStartOfDay();
                    break;
                case "week":
                    startTime = today.minusDays(7).atStartOfDay();
                    break;
                case "month":
                default:
                    startTime = today.minusDays(30).atStartOfDay();
                    break;
            }
            wrapper.ge(EmotionRecord::getCreateTime, startTime);
        }

        Page<EmotionRecord> page = new Page<>(pageNum, pageSize);
        Page<EmotionRecord> result = emotionRecordMapper.selectPage(page, wrapper);

        PageResult<EmotionRecordBriefVo> pageResult = new PageResult<>();
        pageResult.setTotal(result.getTotal());
        pageResult.setRecords(result.getRecords().stream().map(record -> {
            EmotionRecordBriefVo vo = new EmotionRecordBriefVo();
            vo.setId(record.getId());
            vo.setSentimentScore(record.getSentimentScore());
            vo.setEmotionLabel(record.getEmotionLabel());
            vo.setRiskLevel(record.getRiskLevel());
            vo.setAnalysisDetail(record.getAnalysisDetail());
            vo.setCreateTime(record.getCreateTime());
            return vo;
        }).toList());

        return Result.ok(pageResult);
    }
}
