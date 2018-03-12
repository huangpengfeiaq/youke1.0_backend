package com.media.ops.backend.dao.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.media.ops.backend.dao.entity.Material;

public interface MaterialMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Material record);

    int insertSelective(Material record);

    Material selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Material record);

    int updateByPrimaryKey(Material record);
    
    List<Material> selectByGroupId(Integer groupId);
    
    List<Material> selectList();
    
    List<Material> selectByNameGroupId(@Param("materialName") String materialName,@Param("groupId") Integer groupId);
}