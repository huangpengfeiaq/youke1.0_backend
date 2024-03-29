package com.media.ops.backend.dao.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.media.ops.backend.dao.entity.Devicegroup;

public interface DevicegroupMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Devicegroup record);

    int insertSelective(Devicegroup record);

    Devicegroup selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Devicegroup record);

    int updateByPrimaryKey(Devicegroup record);
    
    List<Devicegroup> selectGroupChildrenByParentId(Integer parentId);
    
    List<Devicegroup> selectGroupsByName(@Param("groupName")String groupName);
    
    int checkExistName(@Param("groupName")String groupName);
    
    int checkExistNameNotSelf(@Param("groupName")String groupName, @Param("groupId")Integer groupId);
}