package com.xtwy.handler;



import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;

import com.alibaba.fastjson.JSONObject;
import com.xtwy.client.core.DefaultFuture;
import com.xtwy.client.param.Response;


public class SimpleClientHandler extends ChannelInboundHandlerAdapter {
//是异步接受的数据，需要将数据返回到主线程
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		// TODO Auto-generated method stub
		if("ping".equals(msg.toString())) {
			ctx.channel().writeAndFlush("ping\r\n");
			return;
		}
		Response response = null;

	    // 假设用 FastJSON 反序列化，替换为你的实际反序列化代码
	    response = JSONObject.parseObject(msg.toString(), Response.class);
	    System.out.println("接收服务器返回数据："+ JSONObject.toJSONString(response));
		DefaultFuture.recieve(response);
		//ctx.channel().attr(AttributeKey.valueOf("sssss")).set(msg);
		
	}

//	@Override
//	public void userEventTriggered(ChannelHandlerContext ctx, Object msg) throws Exception {
//	}
	
}
