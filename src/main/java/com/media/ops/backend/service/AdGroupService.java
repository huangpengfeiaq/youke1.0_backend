package com.media.ops.backend.service;

import java.util.List;

import com.media.ops.backend.dao.entity.Adgroup;
import com.media.ops.backend.util.ResponseEntity;

public interface AdGroupService {

	ResponseEntity<String> addGroup(String groupName, Integer parentId);
	
	ResponseEntity<String> updateGroupName(Integer groupId, String groupName);
	
	ResponseEntity<String> deleteGroup(Integer groupId);
	
	ResponseEntity<List<Adgroup>> getChildParallelGroup(Integer groupId);
	
	ResponseEntity<List<Adgroup>> getChildParallelGroup(Integer groupId, String sortField,String sortRule);
	
	ResponseEntity<List<Integer>> selectGroupAndChildrenById(Integer groupId);
}