package com.sso.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mysql.jdbc.StringUtils;
import com.sso.common.ExceptionUtil;
import com.sso.common.SSOResult;
import com.sso.pojo.TbUser;
import com.sso.service.UserService;

@RestController
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private UserService userService;
	
	private SSOResult result=null;
	@RequestMapping("/regist")
	public SSOResult regist(TbUser user){
		try {
			return userService.createUser(user);
		} catch (Exception e) {
			e.printStackTrace();
			return SSOResult.build(500, "服务器异常");
		}
		
	}
	
	@RequestMapping("/check/{param}/{type}")
	public Object checkDate(@PathVariable String param,@PathVariable Integer type,String callback) {
		if(StringUtils.isNullOrEmpty(param)) {
			result=SSOResult.build(400, "校验内容不能为空");
		}
		if(type==null) {
			result =SSOResult.build(400, "校验类型不能为空");
		}
		if(type!=1&&type!=2&&type!=3) {
			result= SSOResult.build(400, "校验类型错误");
		}
		if(null!=result) {
			if(null!=callback) {
				MappingJacksonValue mappingJacksonValue=new MappingJacksonValue(result);
				mappingJacksonValue.setJsonpFunction(callback);
				return mappingJacksonValue;
			}else {
				return result;
			}
		}
		try {
			result=userService.checkData(param, type);
		} catch (Exception e) {
			result=SSOResult.build(500, "服务器发生异常");
		}
		if (null != callback) {
			MappingJacksonValue mappingJacksonValue = new MappingJacksonValue(result);
			mappingJacksonValue.setJsonpFunction(callback);
			return mappingJacksonValue;
		} else {
			return result; 
		}
	}
	//用户注册
	@RequestMapping(value="/register",method=RequestMethod.POST)
	public SSOResult createUser(TbUser user) {
		try {
			SSOResult result=userService.createUser(user);
			return result;
		} catch (Exception e) {
			return SSOResult.build(500, "服务器异常");
		}
	}
	
	@RequestMapping(value="/login",method=RequestMethod.POST)
	public SSOResult userLogin(String username, String password,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			SSOResult result = userService.userLogin(username, password, request, response);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return SSOResult.build(500, ExceptionUtil.getStackTrace(e));
		}
	}
	@RequestMapping("/token/{token}")
	public Object getUserByToken(@PathVariable String token, String callback) {
		SSOResult result = null;
		try {
			result = userService.getUserByToken(token);
		} catch (Exception e) {
			e.printStackTrace();
			result = SSOResult.build(500, ExceptionUtil.getStackTrace(e));
		}
		
		//判断是否为jsonp调用
		if (StringUtils.isNullOrEmpty(callback)) {
			return result;
		} else {
			MappingJacksonValue mappingJacksonValue = new MappingJacksonValue(result);
			mappingJacksonValue.setJsonpFunction(callback);
			return mappingJacksonValue;
		}
		
	}
}
