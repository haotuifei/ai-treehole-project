package com.zxw.treehole.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zxw.treehole.entity.CounselorClass;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CounselorClassMapper extends BaseMapper<CounselorClass> {

    @Delete("DELETE FROM counselor_class WHERE counselor_user_id = #{counselorUserId}")
    int physicalDeleteByCounselorId(Long counselorUserId);
}
