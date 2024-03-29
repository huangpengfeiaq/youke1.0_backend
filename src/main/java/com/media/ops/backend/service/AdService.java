package com.media.ops.backend.service;

import com.media.ops.backend.controller.request.AdAddRequestBean;
import com.media.ops.backend.controller.request.AdMergeUptRequestBean;
import com.media.ops.backend.controller.request.AdUptRequestBean;
import com.media.ops.backend.controller.response.PageResponseBean;
import com.media.ops.backend.util.ResponseEntity;
import com.media.ops.backend.vo.AdVo;

public interface AdService {

	public ResponseEntity  addAd(String createby, AdAddRequestBean bean);
	
	public ResponseEntity delAd(Integer adId, String updateby);
	
	public ResponseEntity  uptAdName(String updateby, AdUptRequestBean bean);
	
	public PageResponseBean<AdVo> selectAdList(Integer pageNum,Integer pageSize);
	
	public PageResponseBean<AdVo> selectAdByKeywordGroup(String keyword, Integer groupId,
			Integer pageNum, Integer pageSize, String beginTime, String endTime);
	
	public ResponseEntity<AdVo> selectAd(Integer id);
	
	public ResponseEntity uptAd(String updateby, AdMergeUptRequestBean bean);
}
