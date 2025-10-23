package com.dxfx.netty.handler;

import com.alibaba.fastjson.JSONObject;
import com.dxfx.netty.handler.param.ServerRequest;
import com.dxfx.netty.medium.Media;
import com.dxfx.netty.util.Response;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

public class ServerHandler extends ChannelInboundHandlerAdapter {
@Override
public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
	//得到的request
	System.out.println("得到request"+msg.toString());
	ServerRequest request = JSONObject.parseObject(msg.toString(), ServerRequest.class);
	
	Media media = Media.newInstance();
	Response resp = media.process(request);
    // 3. 序列化为 JSON 字符串，并拼接分隔符 \r\n（与客户端解码器匹配）
    String jsonResp = JSONObject.toJSONString(resp) + "\r\n"; // 合并为一个字符串
    
    // 4. 一次性发送完整消息
    ctx.channel().writeAndFlush(jsonResp);
}


//心跳机制
//userEventTriggered 是 Netty 中 ChannelInboundHandlerAdapter 的方法，用于处理用户自定义事件或Netty 内置事件（如这里的空闲状态事件）。
//处理通道来的空闲事件，也就是超过多少秒通道没有读东西或者写东西
@Override
public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
	if(evt instanceof IdleStateEvent){
		IdleStateEvent event = (IdleStateEvent)evt;
		if(event.state().equals(IdleState.READER_IDLE)) {
			System.out.println("读空闲");
			//认为客户端可能不可用
			ctx.channel().close();
		}else if(event.state().equals(IdleState.WRITER_IDLE)) {
			System.out.println("写空闲");
		}else if(event.state().equals(IdleState.ALL_IDLE)) {
			System.out.println("都空闲");
			ctx.channel().writeAndFlush("ping\r\n");
		}
	}
}


}
