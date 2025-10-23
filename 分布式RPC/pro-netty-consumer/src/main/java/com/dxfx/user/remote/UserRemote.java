package com.dxfx.user.remote;

import java.util.List;

import com.dxfx.user.bean.User;
import com.xtwy.client.param.Response;

public interface UserRemote {
	// 相当于一个菜单，告诉我们能调用什么方法
	public Response saveUser(User user);
	public Response saveUsers(List<User> users);
}
