package com.media.ops.backend.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.media.ops.backend.contants.Const;
import com.media.ops.backend.controller.request.AddeliveryAddRequestBean;
import com.media.ops.backend.controller.request.AddeliveryEmergentRequestBean;
import com.media.ops.backend.controller.request.AddeliveryUptRequestBean;
import com.media.ops.backend.controller.request.PageRequestBean;
import com.media.ops.backend.controller.response.PageResponseBean;
import com.media.ops.backend.dao.entity.Ad;
import com.media.ops.backend.dao.entity.Addelivery;
import com.media.ops.backend.dao.entity.Area;
import com.media.ops.backend.dao.entity.Building;
import com.media.ops.backend.dao.entity.City;
import com.media.ops.backend.dao.entity.Devicegroup;
import com.media.ops.backend.dao.mapper.AdMapper;
import com.media.ops.backend.dao.mapper.AddeliveryMapper;
import com.media.ops.backend.dao.mapper.AreaMapper;
import com.media.ops.backend.dao.mapper.BuildingMapper;
import com.media.ops.backend.dao.mapper.CityMapper;
import com.media.ops.backend.dao.mapper.DevicegroupMapper;
import com.media.ops.backend.service.AddeliveryService;
import com.media.ops.backend.util.ResponseEntity;
import com.media.ops.backend.util.ResponseEntityUtil;
import com.media.ops.backend.vo.AddeliveryDetailVo;



@Service
public class AddeliveryServiceImpl implements AddeliveryService {

	@Autowired
	private AddeliveryMapper addeliveryMapper;
	@Autowired
	private AdMapper adMapper;
	@Autowired
	private CityMapper cityMapper;
	@Autowired
	private AreaMapper areaMapper;
	@Autowired
	private DevicegroupMapper devicegroupMapper;
	@Autowired
	private BuildingMapper buildingMapper;
	
	public ResponseEntity createAdDelivery(String createby, AddeliveryAddRequestBean bean) {
		
		Addelivery addelivery =new Addelivery();
		addelivery.setAdid(bean.getAdid());
		addelivery.setAdtype(bean.getAdtype());
		addelivery.setDelivertype(bean.getDelivertype());
		addelivery.setAreaid(bean.getAreaid());
		addelivery.setGroupid(bean.getGroupid());
		addelivery.setBegintime(bean.getBegintime());
		addelivery.setEndtime(bean.getEndtime());
		addelivery.setCreateBy(createby);
		addelivery.setUpdateBy(createby);
		
		int resultCount= addeliveryMapper.insertSelective(addelivery);
		return ResponseEntityUtil.addMessage(resultCount);
	}
	
	public ResponseEntity updateAdDelivery(String updateby, AddeliveryUptRequestBean bean) {
        Addelivery uptAddelivery= new Addelivery();
        uptAddelivery.setId(bean.getId());
        uptAddelivery.setAdtype(bean.getAdtype());
        uptAddelivery.setDelivertype(bean.getDelivertype());
        uptAddelivery.setAreaid(bean.getAreaid());
        uptAddelivery.setGroupid(bean.getGroupid());
        uptAddelivery.setBegintime(bean.getBegintime());
        uptAddelivery.setEndtime(bean.getEndtime());
        uptAddelivery.setUpdateBy(updateby);
        
        int resultCount= addeliveryMapper.updateByPrimaryKeySelective(uptAddelivery);
       
        return ResponseEntityUtil.updMessage(resultCount);
	}

	@Override
	public ResponseEntity emergentAdDelivery(String account, AddeliveryEmergentRequestBean bean) {
		Addelivery uptAddelivery= new Addelivery();
        uptAddelivery.setId(bean.getId());
        uptAddelivery.setAdtype(Const.AdTypeEnum.EMERGENT_PLAY);
        uptAddelivery.setBegintime(bean.getBegintime());
        uptAddelivery.setEndtime(bean.getEndtime());
        uptAddelivery.setUpdateBy(account);
        
        int resultCount= addeliveryMapper.updateByPrimaryKeySelective(uptAddelivery);
       
        return ResponseEntityUtil.updMessage(resultCount);
	}

	@Override
	public PageResponseBean<AddeliveryDetailVo> selectList(PageRequestBean bean) {
		PageHelper.startPage(bean.getPageNum(), bean.getPageSize());
		List<Addelivery> addeliveries= addeliveryMapper.selectList();
		List<AddeliveryDetailVo> addeliveryDetailVos= Lists.newArrayList();
		for(Addelivery addelivery: addeliveries) {
			AddeliveryDetailVo addeliveryDetailVo= assembleAddeliveryDetailVo(addelivery);
			addeliveryDetailVos.add(addeliveryDetailVo);
		}
		PageInfo pageInfo=new PageInfo(addeliveries);
		pageInfo.setList(addeliveryDetailVos);
		
		return new PageResponseBean<AddeliveryDetailVo>(pageInfo);
	}
	
	private AddeliveryDetailVo assembleAddeliveryDetailVo(Addelivery addelivery) {
		AddeliveryDetailVo addeliveryDetailVo=new AddeliveryDetailVo();
		addeliveryDetailVo.setId(addelivery.getId());
		
		Ad ad= adMapper.selectByPrimaryKey(addelivery.getAdid());
		if(ad!=null) {
			addeliveryDetailVo.setAdid(ad.getId());
			addeliveryDetailVo.setAdName(ad.getName());
		}
		
	    addeliveryDetailVo.setAdtype(addelivery.getAdtype());
	    addeliveryDetailVo.setDelivertype(addelivery.getDelivertype());
	    
        //获取区域名称，城市名称
	    Area area= areaMapper.selectByAreaId(addelivery.getAreaid());
	    if(area!=null) {
	    	addeliveryDetailVo.setAreaid(area.getAreaid());
	    	addeliveryDetailVo.setAreaName(area.getArea());
	    }
	    City city=cityMapper.selectByCityId(addelivery.getAreaid().substring(0, 4)+"00");
	    if(city!=null) {
	    	addeliveryDetailVo.setCityName(city.getCity());
	    }
	    
	    addeliveryDetailVo.setBuildingOrGroupId(addelivery.getGroupid());
	    if(addelivery.getDelivertype().intValue()== Const.AdDeliveryTypeEnum.BY_BUILDING) {
	    	Building building= buildingMapper.selectByPrimaryKey(addelivery.getGroupid());
	        addeliveryDetailVo.setBuildingOrGroupName(building.getName());
	    }else {
	    	Devicegroup devicegroup= devicegroupMapper.selectByPrimaryKey(addelivery.getGroupid());
	    	addeliveryDetailVo.setBuildingOrGroupName(devicegroup.getName());
	    }
	    
	    addeliveryDetailVo.setBegintime(addelivery.getBegintime());
	    addeliveryDetailVo.setEndtime(addelivery.getEndtime());
	
		return addeliveryDetailVo;
	}
	
}
