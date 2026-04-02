package com.zxw.treehole.service;

import com.zxw.treehole.common.PageResult;
import com.zxw.treehole.dto.CounselorWarningPageQuery;
import com.zxw.treehole.dto.InterventionCreateRequest;
import com.zxw.treehole.dto.WarningStatusUpdateRequest;
import com.zxw.treehole.vo.WarningDetailVo;
import com.zxw.treehole.vo.WarningListItemVo;

public interface CounselorWarningService {

    PageResult<WarningListItemVo> pageWarnings(Long counselorUserId, CounselorWarningPageQuery query);

    WarningDetailVo getDetail(Long counselorUserId, Long warningId);

    void updateStatus(Long counselorUserId, Long warningId, WarningStatusUpdateRequest request);

    void addIntervention(Long counselorUserId, Long warningId, InterventionCreateRequest request);
}
