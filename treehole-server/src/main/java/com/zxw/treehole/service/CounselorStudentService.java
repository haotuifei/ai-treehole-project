package com.zxw.treehole.service;

import com.zxw.treehole.common.PageResult;
import com.zxw.treehole.dto.CounselorStudentPageQuery;
import com.zxw.treehole.vo.StudentDetailVo;
import com.zxw.treehole.vo.StudentProfileVo;

public interface CounselorStudentService {

    PageResult<StudentProfileVo> pageStudents(Long counselorUserId, CounselorStudentPageQuery query);

    StudentDetailVo getStudentDetail(Long counselorUserId, Long studentId);
}
