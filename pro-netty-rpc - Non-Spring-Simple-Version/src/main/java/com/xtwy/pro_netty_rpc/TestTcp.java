package com.xtwy.pro_netty_rpc;

import org.junit.Test;

import com.dxfx.netty.client.ClientRequest;
import com.dxfx.netty.client.Response;
import com.dxfx.netty.client.TcpClient;

public class TestTcp {
	@Test
	public void testGetResponse() {
		ClientRequest request = new ClientRequest();
		request.setContent("这是client的长连接请求");
		Response resp = TcpClient.send(request);
		System.out.println(resp.getResult());
	}
}
