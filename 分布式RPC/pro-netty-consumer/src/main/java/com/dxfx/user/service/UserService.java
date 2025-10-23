package com.dxfx.user.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.dxfx.user.bean.User;

//被 @Service 修饰的类通常包含核心业务处理逻辑（如数据校验、事务处理、调用 DAO 层操作数据库等），是 MVC 分层架构中 “Service 层” 的核心标识。
@Service
public class UserService {

	public void save(User user) {
		
		// TODO Auto-generated method stub
		//访问MySQL数据库
		System.out.println("正在访问数据库，已保存用户"+user);
	}
	public void saveList(List<User> users) {
		
		// TODO Auto-generated method stub
		//访问MySQL数据库
		System.out.println("正在访问数据库，已保存一组用户"+users);
	}
}
