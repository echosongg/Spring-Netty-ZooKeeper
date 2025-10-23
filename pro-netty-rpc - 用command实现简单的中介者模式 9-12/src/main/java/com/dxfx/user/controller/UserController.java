package com.dxfx.user.controller;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;

import com.dxfx.netty.util.Response;
import com.dxfx.netty.util.ResponseUtil;
import com.dxfx.user.bean.User;
import com.dxfx.user.service.UserService;

@Controller

//被 @Controller 修饰的类会被 Spring 容器识别为请求处理器，负责接收 HTTP 请求（如浏览器、客户端的请求）。
//写业务代码
public class UserController {
	@Resource
	private UserService userService;
	
	public Response saveUser(User user) {
		userService.save(user);
		return ResponseUtil.createSuccessResult(user);
	}
	
	public Response saveUsers(List<User> users) {
		userService.saveList(users);
		return ResponseUtil.createSuccessResult(users);
	}
}
