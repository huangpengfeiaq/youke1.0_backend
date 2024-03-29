package com.media.ops.backend.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.media.ops.backend.contants.Const;
import com.media.ops.backend.contants.Errors;
import com.media.ops.backend.dao.entity.Device;
import com.media.ops.backend.dao.entity.Devicegroup;
import com.media.ops.backend.dao.mapper.DeviceMapper;
import com.media.ops.backend.dao.mapper.DevicegroupMapper;
import com.media.ops.backend.service.DeviceGroupService;
import com.media.ops.backend.util.ListSortUtil;
import com.media.ops.backend.util.ResponseEntity;
import com.media.ops.backend.util.ResponseEntityUtil;
import com.media.ops.backend.vo.DeviceGroupVo;

@Service
public class DeviceGroupServiceImpl implements DeviceGroupService {
	
	private  Logger log = LoggerFactory.getLogger(DeviceGroupServiceImpl.class);

	@Autowired
	private DevicegroupMapper devicegroupMapper;
	@Autowired
	private DeviceMapper deviceMapper;

	@Override
	public ResponseEntity addGroup(String groupName, Integer parentId) {

		if(parentId==null || StringUtils.isBlank(groupName)) {
			return ResponseEntityUtil.fail(Errors.SYSTEM_REQUEST_PARAM_ERROR);
		}
		
		if(devicegroupMapper.checkExistName(groupName)>0) {
			return ResponseEntityUtil.fail("同名分组已存在！");
		}		
		
		Devicegroup devicegroup= new Devicegroup();
		devicegroup.setName(groupName);
		devicegroup.setParentid(parentId);
		devicegroup.setStatus(Const.GroupStatusEnum.NORMAL);
		
		int resultCount= devicegroupMapper.insert(devicegroup);
		if(resultCount>0) {
			Map<String, Object> result= Maps.newHashMap();
			result.put("newData", devicegroup);
			return ResponseEntityUtil.success(result);
		}
		
		return ResponseEntityUtil.fail(Errors.SYSTEM_INSERT_FAIL);
      
	}

	@Override
	public ResponseEntity updateGroupName(Integer groupId, String groupName) {
		if(groupId==null || StringUtils.isBlank(groupName)) {
			return ResponseEntityUtil.fail(Errors.SYSTEM_REQUEST_PARAM_ERROR);
		}
		
		if(devicegroupMapper.checkExistNameNotSelf(groupName, groupId)>0) {
			return ResponseEntityUtil.fail("同名分组已存在！");
		}
		
		Devicegroup devicegroup= new Devicegroup();
		devicegroup.setId(groupId);
		devicegroup.setName(groupName);
		
		int resultCount= devicegroupMapper.updateByPrimaryKeySelective(devicegroup);
		
		if(resultCount>0) {
			return ResponseEntityUtil.success(devicegroupMapper.selectByPrimaryKey(devicegroup.getId()));
		}
		return ResponseEntityUtil.fail(Errors.SYSTEM_UPDATE_ERROR);
	}

	@Override
	public ResponseEntity<String> deleteGroup(Integer groupId) {
		if(groupId==null) {
			return ResponseEntityUtil.fail(Errors.SYSTEM_REQUEST_PARAM_ERROR);
		}
		
		List<Device> devices= deviceMapper.selectByGroupId(groupId);
		if(CollectionUtils.isNotEmpty(devices)) {
			return  ResponseEntityUtil.fail("该分组下已有设备，不能删除！");
		}
		
		Devicegroup devicegroup= new Devicegroup();
		devicegroup.setId(groupId);
		devicegroup.setStatus(Const.GroupStatusEnum.ABANDON);
		
		int resultCount= devicegroupMapper.updateByPrimaryKeySelective(devicegroup);
		
		return ResponseEntityUtil.delMessage(resultCount);
	}

	@Override
	public ResponseEntity<List<DeviceGroupVo>> getChildParallelGroup(Integer groupId) {
		List<Devicegroup> devicegroups= devicegroupMapper.selectGroupChildrenByParentId(groupId);
		if(CollectionUtils.isEmpty(devicegroups)) {
			log.info("未找到当前分类的子分类");
		}
		
		List<DeviceGroupVo> deviceGroupVoList= Lists.newArrayList();
		for(Devicegroup devicegroup: devicegroups) {
			DeviceGroupVo deviceGroupVo= this.assembleDeviceGroupVo(devicegroup);
			deviceGroupVoList.add(deviceGroupVo);
		}
		
		
		return ResponseEntityUtil.success(deviceGroupVoList);
	}
	
	@Override
	public ResponseEntity<List<DeviceGroupVo>> getChildParallelGroup(Integer groupId, String sortField, String sortRule) {
		List<Devicegroup> devicegroups= devicegroupMapper.selectGroupChildrenByParentId(groupId);
		if(CollectionUtils.isEmpty(devicegroups)) {
			log.info("未找到当前分类的子分类");
		}
		
		new ListSortUtil<Devicegroup>().Sort(devicegroups, sortField, sortRule);
		
		List<DeviceGroupVo> deviceGroupVoList= Lists.newArrayList();
		for(Devicegroup devicegroup: devicegroups) {
			DeviceGroupVo deviceGroupVo= this.assembleDeviceGroupVo(devicegroup);
			deviceGroupVoList.add(deviceGroupVo);
		}
		
		return ResponseEntityUtil.success(deviceGroupVoList);
	}

	
	private DeviceGroupVo assembleDeviceGroupVo(Devicegroup devicegroup) {
		DeviceGroupVo deviceGroupVo= new DeviceGroupVo();
		deviceGroupVo.setId(devicegroup.getId());
		deviceGroupVo.setName(devicegroup.getName());
		deviceGroupVo.setParentid(devicegroup.getParentid());
		return deviceGroupVo;
	}
	
	//递归算法，算出子节点
	private Set<Devicegroup> findChildCategory(Set<Devicegroup> devicegroupSet, Integer groupId){
		Devicegroup devicegroup= devicegroupMapper.selectByPrimaryKey(groupId);
		if(devicegroup!=null) {
			devicegroupSet.add(devicegroup);
		}
		//查找子节点，递归算法一定要有一个退出的条件
		List<Devicegroup> devicegroups= devicegroupMapper.selectGroupChildrenByParentId(groupId);
		for(Devicegroup groupItem: devicegroups) {
			findChildCategory(devicegroupSet, groupItem.getId());
		}
		return devicegroupSet;
	}

	@Override
	public ResponseEntity<List<Integer>> selectGroupAndChildrenById(Integer groupId) {
		Set<Devicegroup> devicegroups= Sets.newHashSet();
		findChildCategory(devicegroups, groupId);
		
		List<Integer> groupIdList= Lists.newArrayList();
		if(groupId!=null) {
			for(Devicegroup devicegroup: devicegroups) {
				groupIdList.add(devicegroup.getId());
			}
		}
		return ResponseEntityUtil.success(groupIdList);
	}

	@Override
	public ResponseEntity<List<DeviceGroupVo>> searchGroupsbyName(String groupName) {
		if(StringUtils.isBlank(groupName)) {
			return ResponseEntityUtil.fail(Errors.SYSTEM_REQUEST_PARAM_ERROR);
		}
		
		groupName=new StringBuilder().append("%").append(groupName).append("%").toString();
		List<Devicegroup> devicegroups= devicegroupMapper.selectGroupsByName(groupName);
		List<DeviceGroupVo> deviceGroupVos=Lists.newArrayList();
		for (Devicegroup devicegroup : devicegroups) {
			DeviceGroupVo deviceGroupVo=assembleDeviceGroupVo(devicegroup);
			deviceGroupVos.add(deviceGroupVo);
		}
		
		return ResponseEntityUtil.success(deviceGroupVos);
	}


}
