package com.media.ops.backend.dao.mapper;

import java.util.List;

import com.media.ops.backend.dao.entity.Floordevice;

public interface FloordeviceMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Floordevice record);

    int insertSelective(Floordevice record);

    Floordevice selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Floordevice record);

    int updateByPrimaryKey(Floordevice record);
    
    List<Floordevice>  selectByFloorno(Integer floorNo);
}