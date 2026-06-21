package com.skill.skill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.skill.skill.entity.SkillFile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface SkillFileMapper extends BaseMapper<SkillFile> {

    @Select("SELECT * FROM skill_file WHERE group_id = #{groupId} AND folder_name = #{folderName} AND status = 1")
    List<SkillFile> selectByGroupAndFolder(@Param("groupId") Long groupId, @Param("folderName") String folderName);

    @Select("SELECT * FROM skill_file WHERE group_id = #{groupId} AND status = 1")
    List<SkillFile> selectByGroupId(@Param("groupId") Long groupId);
}
