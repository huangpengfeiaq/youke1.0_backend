package com.media.ops.backend.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.media.ops.backend.contants.Const;
import com.media.ops.backend.contants.Errors;
import com.media.ops.backend.controller.request.AdAddRequestBean;
import com.media.ops.backend.controller.request.AdMaterialUptRequestBean;
import com.media.ops.backend.controller.request.AdMergeUptRequestBean;
import com.media.ops.backend.controller.request.AdUptRequestBean;
import com.media.ops.backend.controller.response.PageResponseBean;
import com.media.ops.backend.dao.entity.Ad;
import com.media.ops.backend.dao.entity.Adgroup;
import com.media.ops.backend.dao.entity.Admaterial;
import com.media.ops.backend.dao.entity.Material;
import com.media.ops.backend.dao.mapper.*;
import com.media.ops.backend.service.AdMaterialService;
import com.media.ops.backend.service.AdService;
import com.media.ops.backend.util.ResponseEntity;
import com.media.ops.backend.util.ResponseEntityUtil;
import com.media.ops.backend.vo.AdMaterialVo;
import com.media.ops.backend.vo.AdVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AdServiceImpl implements AdService{

	@Autowired
	private AdMapper adMapper;
	@Autowired
	private AdmaterialMapper admaterialMapper;
	@Autowired
	private AdgroupMapper adgroupMapper;
	@Autowired
	private MaterialMapper materialMapper;
	@Autowired
	private AdMaterialService adMaterialService;
	@Autowired
	private AddeliveryMapper addeliveryMapper;
	
	public ResponseEntity  addAd(String createby, AdAddRequestBean bean) {
		Ad ad=new Ad();
		ad.setCreateBy(createby);
		ad.setUpdateBy(createby);
		ad.setName(bean.getName());
		ad.setGroupid(bean.getGroupid());
		
		int resultCount=adMapper.insert(ad);
		if(resultCount>0) {
			Map<String, Object> result= Maps.newHashMap();
			result.put("ad", ad);
			return  ResponseEntityUtil.success(result);
		}

		return ResponseEntityUtil.fail("新建广告失败");
	}
	
	public ResponseEntity delAd(Integer adId, String updateby) {
		Ad uptAd=new Ad();
		uptAd.setDelFlag(Const.DelFlagEnum.DELETED);
		uptAd.setUpdateBy(updateby);
		uptAd.setId(adId);
		int resultCount= adMapper.updateByPrimaryKeySelective(uptAd);
		
		if(resultCount>0) {
			admaterialMapper.batchUpdateDelFlagByAdId(adId, updateby);
			if(addeliveryMapper.checkExistByAdId(adId)>0) {
				addeliveryMapper.batchUpdateDelFlagByAdId(adId, updateby);
			}
		}

		return ResponseEntityUtil.delMessage(resultCount);

	}

	@Override
	public ResponseEntity uptAdName(String updateby, AdUptRequestBean bean) {
		Ad uptAd=new Ad();
		uptAd.setUpdateBy(updateby);
		uptAd.setId(bean.getId());
		uptAd.setName(bean.getName());
		int resultCount= adMapper.updateByPrimaryKeySelective(uptAd);
		return ResponseEntityUtil.updMessage(resultCount);
	}
	
	public ResponseEntity uptAd(String updateby, AdMergeUptRequestBean bean) {
		Ad uptAd=new Ad();
		uptAd.setUpdateBy(updateby);
		uptAd.setId(bean.getId());
		uptAd.setName(bean.getName());
		uptAd.setGroupid(bean.getGroupid());
		int resultCount= adMapper.updateByPrimaryKeySelective(uptAd);
		
		if(resultCount>0) {
			
			admaterialMapper.batchUpdateDelFlagByAdId(bean.getId(), updateby);
			
			List<AdMaterialUptRequestBean> beans= bean.getAdData();
			for (AdMaterialUptRequestBean adMaterailBean : beans) {
				
				Admaterial admaterial=new Admaterial();
				admaterial.setAdid(adMaterailBean.getAdId());
				admaterial.setMaterialid(adMaterailBean.getMaterialId());
				admaterial.setOrderindex(adMaterailBean.getOrderIndex());
				admaterial.setLoadstep(adMaterailBean.getLoadStep());
				admaterial.setDisplaytime(adMaterailBean.getDisplayTime());
				admaterial.setMusicpath(adMaterailBean.getMusicPath());
				admaterial.setCreateBy(updateby);
				admaterial.setUpdateBy(updateby);
				
				if(adMaterailBean.getId()==0 || admaterialMapper.selectByPrimaryKey(adMaterailBean.getId())==null) {
					admaterialMapper.insertSelective(admaterial);
				}else {
					admaterial.setId(adMaterailBean.getId());
					admaterial.setDelFlag("0");
					admaterialMapper.updateByPrimaryKeySelective(admaterial);
				}
			}
		}
		
		return ResponseEntityUtil.updMessage(resultCount);
	}
	
	public PageResponseBean<AdVo> selectAdList(Integer pageNum,Integer pageSize){
		PageHelper.startPage(pageNum,pageSize);
		List<Ad> adList= adMapper.selectList();
		List<AdVo> adVos= Lists.newArrayList();
		for(Ad ad: adList) {
			List<Admaterial> admaterials= admaterialMapper.selectByAdId(ad.getId());
			List<AdMaterialVo> adMaterialVos=Lists.newArrayList();
			for (Admaterial admaterial : admaterials) {
				AdMaterialVo adMaterialVo= assembleAdMaterialVo(admaterial);
				adMaterialVos.add(adMaterialVo);
			}
			
			AdVo adVo= assembleAdVo(ad, adMaterialVos);
			adVos.add(adVo);
		}
		
		PageInfo pageInfo=new PageInfo(adList);
		pageInfo.setList(adVos);
		PageResponseBean<AdVo> list = new PageResponseBean<AdVo>(pageInfo);
		list.setHttpStatus(200);
		list.setCode(0);
		return list;
	}
	
	public ResponseEntity<AdVo> selectAd(Integer id){
		if(id==null) {
			return ResponseEntityUtil.fail(Errors.SYSTEM_REQUEST_PARAM_ERROR);
		}
		Ad ad=adMapper.selectByPrimaryKey(id);
		if(ad==null) {
			return ResponseEntityUtil.fail(Errors.SYSTEM_DATA_NOT_FOUND);
		}
		List<Admaterial> admaterials= admaterialMapper.selectByAdId(ad.getId());
		List<AdMaterialVo> adMaterialVos=Lists.newArrayList();
		for (Admaterial admaterial : admaterials) {
			AdMaterialVo adMaterialVo= assembleAdMaterialVo(admaterial);
			adMaterialVos.add(adMaterialVo);
		}
		AdVo adVo= assembleAdVo(ad, adMaterialVos);
		
		return ResponseEntityUtil.success(adVo);
	}
	
	private AdVo assembleAdVo(Ad ad, List<AdMaterialVo> adMaterialVos) {
		AdVo adVo=new AdVo();
		adVo.setId(ad.getId());
		adVo.setName(ad.getName());
		adVo.setGroupid(ad.getGroupid());
		
		if(addeliveryMapper.checkDeliveryByAdId(ad.getId())>0) {
			adVo.setEditStatus(0);
		}else {
			adVo.setEditStatus(1);
		}
		
		Adgroup adgroup= adgroupMapper.selectByPrimaryKey(ad.getGroupid());
		if(adgroup!=null) {
			adVo.setGroupName(adgroup.getName());
		}
		adVo.setAdMaterialVos(adMaterialVos);
	
		return adVo;
	}
	
	private AdMaterialVo assembleAdMaterialVo(Admaterial admaterial) {
		AdMaterialVo adMaterialVo=new AdMaterialVo();
		adMaterialVo.setId(admaterial.getId());
		adMaterialVo.setAdId(admaterial.getAdid());
		
		
		Material material= materialMapper.selectByPrimaryKey(admaterial.getMaterialid());
		adMaterialVo.setMaterialId(material.getId());
		adMaterialVo.setMaterialName(material.getName());
		adMaterialVo.setMaterialType(material.getType());
		adMaterialVo.setMaterialPath(material.getPath());
		
		adMaterialVo.setOrderIndex(admaterial.getOrderindex()==null?1:admaterial.getOrderindex());
		adMaterialVo.setLoadStep(admaterial.getLoadstep()==null?0:admaterial.getLoadstep());
		adMaterialVo.setDisplayTime(admaterial.getDisplaytime()==null?0:admaterial.getDisplaytime());
		adMaterialVo.setMusicPath(admaterial.getMusicpath());
		
		return adMaterialVo;
		
	}

	@Override
	public PageResponseBean<AdVo> selectAdByKeywordGroup(String keyword, Integer groupId, Integer pageNum,
			Integer pageSize, String beginTime, String endTime) {
		if(StringUtils.isNotBlank(keyword)) {
			keyword= new StringBuilder().append("%").append(keyword).append("%").toString();
		}
		
		
		PageHelper.startPage(pageNum, pageSize);
		List<Ad> adList= adMapper.selectByNameGroupId(
				StringUtils.isBlank(keyword)?null:keyword,
						groupId==0?null:groupId,
								StringUtils.isBlank(beginTime)?null:beginTime, 
										StringUtils.isBlank(endTime)?null:endTime
								);
		List<AdVo> adVos= Lists.newArrayList();
		for(Ad ad: adList) {
			List<Admaterial> admaterials= admaterialMapper.selectByAdId(ad.getId());
			List<AdMaterialVo> adMaterialVos=Lists.newArrayList();
			for (Admaterial admaterial : admaterials) {
				AdMaterialVo adMaterialVo= assembleAdMaterialVo(admaterial);
				adMaterialVos.add(adMaterialVo);
			}
			
			AdVo adVo= assembleAdVo(ad, adMaterialVos);
			adVos.add(adVo);
		}
		
		PageInfo pageInfo=new PageInfo(adList);
		pageInfo.setList(adVos);
		PageResponseBean<AdVo> list = new PageResponseBean<AdVo>(pageInfo);
		list.setHttpStatus(200);
		list.setCode(0);
		return list;
	}
	
}
