package com.media.ops.backend.service.impl;

import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import com.beust.jcommander.internal.Lists;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.media.ops.backend.contants.Const;
import com.media.ops.backend.contants.Errors;
import com.media.ops.backend.controller.request.PageRequestBean;
import com.media.ops.backend.controller.request.PlayAddRequestBean;
import com.media.ops.backend.controller.request.PlayUpdateRequestBean;
import com.media.ops.backend.controller.response.PageResponseBean;
import com.media.ops.backend.dao.entity.Play;
import com.media.ops.backend.dao.entity.User;
import com.media.ops.backend.dao.mapper.PlayMapper;
import com.media.ops.backend.dao.mapper.UserMapper;
import com.media.ops.backend.service.PlayService;
import com.media.ops.backend.util.ResponseEntity;
import com.media.ops.backend.util.ResponseEntityUtil;
import com.media.ops.backend.vo.PlayVo;
import com.media.ops.backend.vo.PlayerVo;
import com.media.ops.backend.vo.UserVo;

@Service
public class PlayServiceImpl implements PlayService{
	
	@Resource
	private PlayMapper playMapper;
	@Resource
	private UserMapper userMapper;

	@Override
	public ResponseEntity<List<Play>> GetPlays(String begintime, String endtime) {
		List<Play> plays=playMapper.selectByTime(begintime, endtime);
		 if (plays==null){
	            return ResponseEntityUtil.fail("查询直播信息失败");
	     }
	        return ResponseEntityUtil.success(plays);
	}

	@Override
	public ResponseEntity<String> add(String createby,PlayAddRequestBean bean) {
		if (bean==null) {
			return ResponseEntityUtil.fail(Errors.SYSTEM_REQUEST_PARAM_ERROR);
		}
		 Play play=new Play();
		 play.setPicpath(bean.getPicpath());
		 play.setCreateBy(createby);
		 play.setPlayaddress(bean.getPlayaddress());
		 play.setPlayerid(bean.getPlayerid());
		 play.setRealaddress(bean.getRealaddress());
		 play.setStatus(bean.getStatus());
		 play.setTitle(bean.getTitle());
		 play.setStreamaddress(bean.getStreamaddress());
		 play.setType(bean.getType());
		 play.setUpdateBy(createby);
		 play.setDelFlag(Const.DelFlagEnum.NORMAL);
		 play.setBegintime(bean.getBegintime());
		 play.setEndtime(bean.getEndtime());
		 int resultCount=playMapper.insertSelective(play);
		return ResponseEntityUtil.addMessage(resultCount);
	}

	@Override
	public ResponseEntity<String> update(String updateby,PlayUpdateRequestBean bean) {
		if (bean==null) {
			return ResponseEntityUtil.fail(Errors.SYSTEM_REQUEST_PARAM_ERROR);
		}
		 Play play=new Play();
		 play.setPicpath(bean.getPicpath());
		 play.setRealaddress(bean.getRealaddress());
		 play.setStatus(bean.getStatus());
		 play.setTitle(bean.getTitle());
		 play.setStreamaddress(bean.getStreamaddress());
		 play.setType(bean.getType());
		 play.setUpdateBy(updateby);
		 play.setId(bean.getId());
		 play.setTitle(bean.getTitle());
		 play.setPlayaddress(bean.getPlayaddress());
		 play.setBegintime(bean.getBegintime());
		 play.setEndtime(bean.getEndtime());
		 play.setPlayerid(bean.getPlayerid());
		 
		 int resultCount=playMapper.updateByPrimaryKeySelective(play);
		 return ResponseEntityUtil.updMessage(resultCount);
	}

	@Override
	public ResponseEntity<String> delete(String updateby,Integer id) {
		if (id==null) {
			return ResponseEntityUtil.fail(Errors.SYSTEM_REQUEST_PARAM_ERROR);
		}
		Play play=new Play();
		play.setId(id);
		play.setUpdateBy(updateby);
		play.setDelFlag(Const.DelFlagEnum.DELETED);
		int resultCount=playMapper.updateByPrimaryKeySelective(play);
		return ResponseEntityUtil.delMessage(resultCount);
	}

	@Override
	public ResponseEntity<PlayVo> selectByKey(Integer id) {
		if (id==null) {
			return ResponseEntityUtil.fail(Errors.SYSTEM_REQUEST_PARAM_ERROR);
		}
		Play play=playMapper.selectByPrimaryKey(id);
		if (play==null) {
			return ResponseEntityUtil.fail("该直播不存在或已删除");
		}
		PlayVo playVo=assemblePlayVo(play);
		return ResponseEntityUtil.success(playVo);
	}
	
	
	private PlayVo assemblePlayVo(Play play){
		PlayVo playVo=new PlayVo();
		
		playVo.setTitle(play.getTitle());
		playVo.setType(play.getType());
		
		
		User user= userMapper.selectByPrimaryKey(play.getPlayerid());
		if(user!=null) {
			playVo.setPlayerid(play.getPlayerid());
			PlayerVo playerVo= new PlayerVo();
			playerVo.setAccount(user.getAccount());
			playerVo.setEmail(user.getEmail());
			playerVo.setPhone(user.getPhone());
			playerVo.setTruename(user.getTruename());
			
			playVo.setPlayerVo(playerVo);
		}
		
		playVo.setRealaddress(play.getRealaddress());
		playVo.setPicpath(play.getPicpath());
		playVo.setBegintime(play.getBegintime());
		playVo.setEndtime(play.getEndtime());
		playVo.setReplayaddress(play.getReplayaddress());
		playVo.setPlayaddress(play.getPlayaddress());
		playVo.setStreamaddress(play.getStreamaddress());
		
		playVo.setStatus(play.getStatus());
		
		return playVo;
	}

	@Override
	public PageResponseBean<PlayVo> selectPlayListByPlayerId(int pageNum, int pageSize,Integer playerId) {
		PageHelper.startPage(pageNum, pageSize);
		List<Play> plays=(List<Play>) playMapper.selectByPlayerId(playerId);
		List<PlayVo> playVos=Lists.newArrayList();
		for (Play play : plays) {
			PlayVo playVo2=assemblePlayVo(play);
			playVos.add(playVo2);
		}
		PageInfo<PlayVo> pageInfo=new PageInfo<>();
		pageInfo.setList(playVos);
		return new PageResponseBean<PlayVo>(pageInfo);
	}

	@Override
	public ResponseEntity<List<PlayVo>> selectPlayListWithStatusAndPlayerId(Integer playerId, Integer status) {
		if (playerId==null || status == null) {
			return ResponseEntityUtil.fail(Errors.SYSTEM_REQUEST_PARAM_ERROR);
		}
		List<Play> plays=playMapper.selectByPlayIdAndStatus(playerId, status);
		if (plays.size()==0) {
			return ResponseEntityUtil.fail(Errors.SYSTEM_DATA_NOT_FOUND);
		}
		List<PlayVo> playVos=Lists.newArrayList();
		for (Play play : plays) {
			PlayVo playVo=assemblePlayVo(play);
			playVos.add(playVo);
		}
		return ResponseEntityUtil.success(playVos);
	}
	
	@Override
	public ResponseEntity<List<PlayVo>> selectPlayList() {
		
		List<Play> plays=playMapper.selectList();
		if (plays.size()==0) {
			return ResponseEntityUtil.fail(Errors.SYSTEM_DATA_NOT_FOUND);
		}
		List<PlayVo> playVos=Lists.newArrayList();
		for (Play play : plays) {
			PlayVo playVo2=assemblePlayVo(play);
			playVos.add(playVo2);
		}
		return ResponseEntityUtil.success(playVos);
	}

}
