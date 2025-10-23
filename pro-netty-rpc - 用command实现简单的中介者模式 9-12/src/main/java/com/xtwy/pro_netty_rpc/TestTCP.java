package com.xtwy.pro_netty_rpc;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.dxfx.netty.client.ClientRequest;
import com.dxfx.netty.client.TcpClient;
import com.dxfx.netty.util.Response;
import com.dxfx.user.bean.User;

public class TestTCP {
	@Test
	public void testSaveUser() {
		ClientRequest request = new ClientRequest();
		User u = new User();
		u.setId(1);
		u.setName("ada");
		request.setContent(u);
		request.setCommand("com.dxfx.user.controller.UserController.saveUser");
		Response resp = TcpClient.send(request);
		System.out.println(resp.getResult());
	}
	
	@Test
	public void testSaveUsers() {
		ClientRequest request = new ClientRequest();
		List<User> users = new ArrayList<User>();
		User u = new User();
		u.setId(1);
		u.setName("ada");
		users.add(u);
		request.setContent(users);
		request.setCommand("com.dxfx.user.controller.UserController.saveUser");
		Response resp = TcpClient.send(request);
		System.out.println(resp.getResult());
	}
}
