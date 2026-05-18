package com.zxw.treehole.service;

import com.zxw.treehole.common.PageResult;
import com.zxw.treehole.dto.CounselorInterventionPageQuery;
import com.zxw.treehole.vo.InterventionListItemVo;

public interface CounselorInterventionService {

    PageResult<InterventionListItemVo> pageInterventions(Long counselorUserId, CounselorInterventionPageQuery query);
}
