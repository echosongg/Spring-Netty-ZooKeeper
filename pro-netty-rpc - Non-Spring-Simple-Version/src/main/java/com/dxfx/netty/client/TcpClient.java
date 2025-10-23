package com.dxfx.netty.client;


import com.alibaba.fastjson.JSONObject;
import com.dxfx.netty.handler.SimpleClientHandler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.AttributeKey;
import io.netty.channel.EventLoopGroup;
/*
 * Netty 客户端的 EventLoopGroup（线程池）、Bootstrap（启动器）和 Channel（连接通道）是 重量级资源：
频繁创建会消耗大量 CPU / 内存（每个 EventLoopGroup 包含多个线程）。
多次连接同一服务端会建立多条 TCP 连接，浪费网络资源。
用 static 修饰后：
static 代码块在类加载时仅执行一次，确保 workerGroup、Bootstrap 和连接（ChannelFuture）只初始化一次。
整个应用中所有调用 TcpClient.send() 的地方，都复用同一条 TCP 连接，符合 “长连接” 的设计初衷。
 */
public class TcpClient {
	static final Bootstrap b = new Bootstrap();
	static ChannelFuture f = null; // (5)
	static {
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		b.group(workerGroup); // (2)
        b.channel(NioSocketChannel.class); // (3)
        b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
        b.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
            	ch.pipeline().addLast(new DelimiterBasedFrameDecoder(Integer.MAX_VALUE,Delimiters.lineDelimiter()[0]));
            	ch.pipeline().addLast(new StringDecoder());
                ch.pipeline().addLast(new SimpleClientHandler());
                ch.pipeline().addLast(new StringEncoder());
            }
        });
        String host = "localhost";
        int port = 8080;
        
        try {
			f = b.connect(host, port).sync();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}     
	//发送数据 长连接
	//每一个请求都是 同一个连接，因为用static修饰了
	//为每一个请求都用一个id来识别和修饰
	public static Response send(ClientRequest request) {
		//这里是主线程，如何通过异步的方式获得从线程得到server回复的response呢
		f.channel().writeAndFlush(JSONObject.toJSONString(request));
		f.channel().writeAndFlush("\r\n");
		//建立一个类，异步的通过锁的方式去获取从线程的数据
		
		DefaultFuture df = new DefaultFuture(request);
		return df.get();
	}
}
