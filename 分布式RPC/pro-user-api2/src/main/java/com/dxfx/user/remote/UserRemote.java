package com.dxfx.user.remote;

import java.util.List;

import com.dxfx.user.model.User;


public interface UserRemote {
	// 相当于一个菜单，告诉我们能调用什么方法
	public Object saveUser(User user);
	public Object saveUsers(List<User> users);
}
