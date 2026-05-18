package com.zxw.treehole.service;

import com.zxw.treehole.common.PageResult;
import com.zxw.treehole.dto.CheckinCreateRequest;
import com.zxw.treehole.dto.CheckinPageQuery;
import com.zxw.treehole.dto.CheckinUpdateRequest;
import com.zxw.treehole.vo.CheckinCalendarVo;
import com.zxw.treehole.vo.CheckinStreakVo;
import com.zxw.treehole.vo.StudyCheckinVo;

public interface StudyCheckinService {

    PageResult<StudyCheckinVo> pageMyCheckins(Long userId, CheckinPageQuery query);

    StudyCheckinVo getMyCheckin(Long userId, Long checkinId);

    Long createCheckin(Long userId, CheckinCreateRequest request);

    void updateCheckin(Long userId, Long checkinId, CheckinUpdateRequest request);

    void deleteCheckin(Long userId, Long checkinId);

    CheckinStreakVo getStreakStats(Long userId);

    CheckinCalendarVo getMonthCalendar(Long userId, String yearMonth);
}
