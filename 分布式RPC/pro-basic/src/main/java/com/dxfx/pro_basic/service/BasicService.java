package com.dxfx.pro_basic.service;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.dxfx.user.model.User;
import com.dxfx.user.remote.UserRemote;
import com.xtwy.annotation.RemoteInvoke;
import com.xtwy.client.param.Response;

@Service
public class BasicService {
	@RemoteInvoke
	private UserRemote userRemote;
	
	public void testSaveUser() {
		User u = new User();
		u.setId(1);
		u.setName("李四");
		Object response = userRemote.saveUser(u);
		System.out.println(JSONObject.toJSONString(response));
	}
}
