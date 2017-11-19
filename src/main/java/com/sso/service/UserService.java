package com.sso.service;

import javax.servlet.http.HttpServletRequest;

import javax.servlet.http.HttpServletResponse;



import com.sso.common.SSOResult;
import com.sso.pojo.TbUser;

public interface UserService {
	SSOResult checkData(String content, Integer type);
	SSOResult createUser(TbUser user);
	SSOResult userLogin(String username, String password, HttpServletRequest request, HttpServletResponse response);
	SSOResult getUserByToken(String token);
	//TbUser selectTeachForGivenName( String username);
}
