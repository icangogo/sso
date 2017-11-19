package com.sso.service.impl;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import com.sso.common.CookieUtils;
import com.sso.common.JsonUtils;
import com.sso.common.SSOResult;
import com.sso.mapper.TbUserMapper;
import com.sso.pojo.TbUser;
import com.sso.pojo.TbUserExample;
import com.sso.pojo.TbUserExample.Criteria;
import com.sso.service.UserService;

import redis.clients.jedis.Jedis;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private TbUserMapper tbUserMapper;

	@Autowired
	private Jedis jedis;

	@Value("${REDIS_SESSION_TOKEN_KEY}")
	private String REDIS_SESSION_TOKEN_KEY;

	@Value("${SSO_SESSION_EXPIRE}")
	private String SSO_SESSION_EXPIRE;

	public SSOResult checkData(String content, Integer type) {
		TbUserExample example = new TbUserExample();
		Criteria criteria = example.createCriteria();
		if (1 == type) {
			criteria.andUsernameEqualTo(content);
		} else if (2 == type) {
			criteria.andPhoneEqualTo(content);
		} else if (3 == type) {
			criteria.andEmailEqualTo(content);
		}
		List<TbUser> list = tbUserMapper.selectByExample(example);
		if (list == null || list.size() == 0) {
			return SSOResult.ok(true);
		}
		return SSOResult.ok(false);
	}

	public SSOResult createUser(TbUser user) {
		user.setUpdated(new Date());
		user.setUpdated(new Date());
		user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));
		tbUserMapper.insert(user);
		return SSOResult.ok();
	}

	public SSOResult userLogin(String username, String password, HttpServletRequest request,
			HttpServletResponse response) {
		TbUserExample example = new TbUserExample();
		Criteria createCriteria = example.createCriteria();
		createCriteria.andUsernameEqualTo(username);
		List<TbUser> list = tbUserMapper.selectByExample(example);
		if (null == list || list.size() == 0) {
			return SSOResult.build(400, "用户名或者密码错误");
		}
		TbUser user = list.get(0);
		if (!DigestUtils.md5DigestAsHex(password.getBytes()).equals(user.getPassword())) {
			return SSOResult.build(400, "用户名或者密码错误");
		}
		String token = UUID.randomUUID().toString();
		user.setPassword(null);
		// 把用户信息写写入到Redis中
		jedis.set(REDIS_SESSION_TOKEN_KEY + ":" + token, JsonUtils.objectToJson(user));
		jedis.expire(REDIS_SESSION_TOKEN_KEY + ":" + token, Integer.parseInt(SSO_SESSION_EXPIRE));
		CookieUtils.setCookie(request, response, "SSO_TOKEN", token);
		return SSOResult.ok(true);
	}

	public SSOResult getUserByToken(String token) {
		// 根据token从redis中查询用户信息
		String json = jedis.get(REDIS_SESSION_TOKEN_KEY + ":" + token);
		// 判断是否为空
		if (StringUtils.isEmpty(json)) {
			return SSOResult.build(400, "此session已经过期，请重新登录");
		}
		// 更新过期时间
		jedis.expire(REDIS_SESSION_TOKEN_KEY + ":" + token, Integer.parseInt(SSO_SESSION_EXPIRE));
		// 返回用户信息
		return SSOResult.ok(JsonUtils.jsonToPojo(json, TbUser.class));
	}

}
