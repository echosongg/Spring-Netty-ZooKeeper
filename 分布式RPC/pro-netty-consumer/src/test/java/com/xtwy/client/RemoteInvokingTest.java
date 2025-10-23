package com.xtwy.client;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.dxfx.user.bean.User;
import com.dxfx.user.remote.UserRemote;
import com.xtwy.annotation.RemoteInvoke;
import com.xtwy.client.param.Response;
import com.alibaba.fastjson.JSONObject;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=RemoteInvokingTest.class)
@ComponentScan("com.dxfx")
public class RemoteInvokingTest {
	
	//会被对象代理 //会被InvokeProxy扫描和修改
	@RemoteInvoke
	private UserRemote userRemote;
	
	@Test
	public void testSaveUser() {
		User u = new User();
		u.setId(1);
		u.setName("李四");
		Response response = userRemote.saveUser(u);
		System.out.println(JSONObject.toJSONString(response));
	}
	
//	@Test
//	public void testSaveUsers() {
//		List<User> users = new ArrayList<User>();
//		User u = new User();
//		u.setId(1);
//		u.setName("ada");
//		users.add(u);
//		userRemote.saveUsers(users);
//	}
}
