package com.media.ops.backend.controller;

import com.media.ops.backend.annotation.ACS;
import com.media.ops.backend.contants.Const;
import com.media.ops.backend.controller.request.*;
import com.media.ops.backend.controller.response.PageResponseBean;
import com.media.ops.backend.dao.entity.User;
import com.media.ops.backend.service.MemcachedService;
import com.media.ops.backend.service.SmsService;
import com.media.ops.backend.service.SysLogService;
import com.media.ops.backend.service.UserService;
import com.media.ops.backend.util.ResponseEntity;
import com.media.ops.backend.util.ResponseEntityUtil;
import com.media.ops.backend.util.StringUtil;
import com.media.ops.backend.vo.SyslogVo;
import com.media.ops.backend.vo.UserVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Api(description="管理员操作接口",produces = "application/json")
@RestController
@RequestMapping("/manager/")
public class UserController extends BaseController {

	@Autowired
	private UserService userService;
	@Autowired
	private SysLogService sysLogService;
	@Autowired
	private SmsService smsService;
	@Resource
	private MemcachedService memcachedService;


	
	@ACS(allowAnonymous = true)
	@ApiOperation(value = "登录接口",notes = "用户登录，username可为账号、手机或邮箱")
	@PostMapping(value="login.do")
	public ResponseEntity<User> login(@Valid @RequestBody UserLoginRequestBean bean,
                                      HttpServletRequest request) {
		
		Boolean flag=false;
		if (memcachedService.get(Const.VERIFY_CODE) != null) {
			String randomCode = memcachedService.get(Const.VERIFY_CODE).toString();

			if (StringUtil.isNotBlank(randomCode) && bean.getVerifyCode().toUpperCase().equals(randomCode.toUpperCase())) {
				flag = true;
				memcachedService.delete(Const.VERIFY_CODE);
			}
		}

		if(!flag) {
			return ResponseEntityUtil.fail("验证码错误");
		}

		ResponseEntity<User> response=userService.adminLogin(bean.getLoginName(), bean.getLoginPwd());
		if(response.isSuccess()) {
			User user= response.getData();
			//session.setAttribute(Const.CURRENT_USER, response.getData());
			// 创建访问token
	        String accessToken = super.generateAccessToken(request);
	        user.setAccessToken(accessToken);

	        super.setAccessTokenAttribute(request, accessToken);
	        super.sessionUser(request, user);

	        return ResponseEntityUtil.success(user);
		}
		return response;
		
        
	}
	
    /**
	 * 退出登录
	 */
	@ApiOperation(value = "退出登录", notes = "退出登录")
	@RequestMapping(value = "/logout", method = RequestMethod.GET)
	public ResponseEntity<Void> logout(HttpServletRequest request) {
		deleteSessionUser(request);
		return ResponseEntityUtil.success();
	}
	
	@ApiOperation(value = "添加管理员接口",notes = "添加管理员")
	@PostMapping(value="add.do")	
	public ResponseEntity add(@Valid @RequestBody UserAddRequestBean bean,
                              HttpServletRequest request){
		User user= new User(bean.getAccount(), bean.getTrueName(), bean.getPassword(),
				bean.getEmail(), bean.getPhone(), bean.getQuestion(), bean.getAnswer(), 
				bean.getType(), (byte) 1, super.getSessionUser(request).getAccount(), super.getSessionUser(request).getAccount());
		
		ResponseEntity response= userService.add(user);
		if(response.isSuccess()) {
			//注册成功，发送短信，告知初始密码
			smsService.send(bean.getPhone(), "您已成为系统的"+ (bean.getType()==0?"管理员":"直播员")+",初始密码为："+bean.getPassword());
		}
		return response;
	}
	
	@ACS(allowAnonymous = true)
	@ApiOperation(value = "有效性校验接口,type可为account或email",notes = "信息校验")
	@PostMapping(value="check_valid.do")
	public ResponseEntity<String> checkValid(@Valid @RequestBody InfoCheckRequestBean bean){
		return userService.checkValid(bean.getValue(),bean.getField(), bean.getType());
	}
	
	
	@ApiOperation(value = "获取管理员信息接口",notes = "获取管理员信息")
	@GetMapping(value="get_user_info.do")
	public ResponseEntity<User> getUserInfo(HttpServletRequest request){
		User user= super.getSessionUser(request);
		if(user !=null) {
			return ResponseEntityUtil.success(user);
		}
		return ResponseEntityUtil.fail("用户未登录，无法获取当前用户信息");
	}
	
//	@ACS(allowAnonymous = true)
//	@ApiOperation(value = "密保提示问题接口",notes = "获取密保提示问题")
//	@PostMapping(value="forget_get_question.do")
//	public ResponseEntity<String> selectQuestion(@RequestBody String username) {
//		return userService.selectQuestion(username);
//	}
//	
//	@ACS(allowAnonymous = true)
//	@ApiOperation(value = "确认密保问题答案接口",notes = "确认密保问题答案")
//	@PostMapping(value="forget_check_answer.do")
//	public ResponseEntity<String> forgetCheckAnswer(@RequestBody PasswordQARequestBean bean){
//		return userService.checkAnswer(bean.getUsername(), bean.getQuestion(), bean.getAnswer());
//	}
//	
//	@ACS(allowAnonymous = true)
//	@ApiOperation(value = "重置密码接口",notes = "重置密码")
//	@PostMapping(value="forget_reset_password.do")
//	public ResponseEntity<String> forgetResetPassword(@RequestBody PasswordQAResetRequestBean bean){
//		return userService.forgetResetPassword(bean.getUsername(), bean.getPasswordNew(), bean.getForgetToken());
//	}
	
	@ApiOperation(value = "登录状态下重置密码接口",notes = "登录状态下重置密码")
	@PostMapping(value="reset_password.do")	
	public ResponseEntity<String> resetPassword(HttpServletRequest request, @Valid @RequestBody PasswordResetRequestBean bean){
	       User user= super.getSessionUser(request);
	       if(user==null) {
	    	   return ResponseEntityUtil.fail("用户未登录");
	       }
	       System.out.println(bean.getPasswordNew()+"------"+bean.getPasswordOld());
	       return userService.resetPassword(bean.getPasswordOld(), bean.getPasswordNew(), user);
	}
	
	@ApiOperation(value = "管理员搜索接口",notes = "管理员搜索")
	@PostMapping(value="search_user.do")		
	public ResponseEntity<User> searchUser(@RequestBody UserSearchRequestBean bean){
		return userService.getUserByAccountEmail(bean.getAccount(), bean.getEmail());
	}

    @ApiOperation(value = "管理员搜索接口",notes = "管理员搜索")
    @PostMapping(value="search_user2.do")
    public PageResponseBean searchUser(@RequestParam String account , @RequestParam Integer pageNum, @RequestParam Integer pageSize){
        return userService.getUserByAccount(pageNum,pageSize,account,null);
    }
	
	@ApiOperation(value = "修改个人信息接口",notes = "修改自身信息")
	@PostMapping(value="update_information.do")
	public ResponseEntity<User> update_information(HttpServletRequest request,
                                                   @Valid @RequestBody UserUptRequestBean bean){
		User myUser= super.getSessionUser(request);
		User user=new User();
		user.setId(myUser.getId());
		user.setAccount(myUser.getAccount());
		
		user.setTruename(bean.getTrueName());
		user.setEmail(bean.getEmail());
		user.setPhone(bean.getPhone());

		if(StringUtils.isNotEmpty(bean.getOldPwd())) {
			if(userService.adminLogin(user.getAccount(), bean.getOldPwd()).isSuccess()) {
				user.setPassword(bean.getNewPwd());
			}
			else {
				return ResponseEntityUtil.fail("旧密码输入错误");
			}
		}

		user.setUpdateBy(user.getAccount());

		ResponseEntity<User> response= userService.updateSelfInfo(user);
		if(response.isSuccess()) {			
			// 创建访问token
	        String accessToken = super.generateAccessToken(request);
	        user.setAccessToken(accessToken);

	        super.setAccessTokenAttribute(request, accessToken);
	        super.sessionUser(request, user);

	        return ResponseEntityUtil.success(user);
		}
		return response;
	}
		
	@ApiOperation(value = "修改他人信息接口",notes = "修改自身信息")
	@PostMapping(value="update_others_infor.do")
	public ResponseEntity<User> updateOthersInfor(HttpServletRequest request,
                                                  @Valid @RequestBody UserInfoUptRequestBean bean){
		User user= new User();
		
		user.setId(bean.getId());
		user.setTruename(bean.getTrueName());
		user.setEmail(bean.getEmail());
		user.setPhone(bean.getPhone());
		user.setType(bean.getType());
		user.setStatus((byte)1);

		user.setUpdateBy(super.getSessionUser(request).getAccount());

		return userService.updateInformation(user);
	}
	@ApiOperation(value = "获取管理员列表",notes = "管理员列表")
	@PostMapping(value="get_list.do")
	public PageResponseBean<UserVo> getList(@RequestParam Integer pageNum, @RequestParam Integer pageSize){
	       return userService.getUserList(pageNum,pageSize);
	}
	
    @ApiOperation(value = "操作记录", notes = "")
    @PostMapping(value = "/records")
    public PageResponseBean<SyslogVo> records(Integer pageNum, Integer pageSize) {
        PageResponseBean page= sysLogService.sysLog(pageNum,pageSize);
        page.setCode(0);
        page.setHttpStatus(200);
        return page;
    }

	@ApiOperation(value = "根据工号或邮箱查询管理员操作日志接口", notes = "根据工号或邮箱查询管理员操作日志")
	@PostMapping(value = "search_syslog.do")
	public PageResponseBean<SyslogVo> searchSyslog(@RequestParam(required = false) String account,@RequestParam(required = false) String email,
												   @RequestParam(defaultValue = "1") Integer pageNum,@RequestParam(defaultValue = "9") Integer pageSize){
		return sysLogService.selectLogbyKey(account,email,pageNum,pageSize);
	}



	@ApiOperation(value = "登录禁用接口",notes = "登录禁用")
	@PostMapping(value="login_forbidden.do")
	public ResponseEntity<String> loginForbidden(@RequestBody UserAccountStatusRequestBean bean){
		return userService.updateStatusById(bean.getAccount(), bean.getStatus());
	}
	
	@ApiOperation(value = "根据id查询管理员信息接口",notes = "根据id查询管理员信息接口")
	@PostMapping(value="get_info.do")	
	public ResponseEntity<UserVo> getInformation(@RequestBody Integer id){
		return userService.getInformation(id);
	}
	

}
