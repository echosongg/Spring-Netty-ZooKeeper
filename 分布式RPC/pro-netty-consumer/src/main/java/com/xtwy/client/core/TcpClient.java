package com.xtwy.client.core;


import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.curator.framework.CuratorFramework;

import com.alibaba.fastjson.JSONObject;
import com.dxfx.client.constant.Constants;
import com.xtwy.client.core.DefaultFuture;
import com.xtwy.client.param.ClientRequest;
import com.xtwy.client.param.Response;
import com.xtwy.client.zk.ZookeeperFactory;
import com.xtwy.handler.SimpleClientHandler;
import org.apache.curator.framework.api.CuratorWatcher;
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
	static ChannelFuture f = null;
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
        
        CuratorFramework client = ZookeeperFactory.create();
        //获取要连接的服务器地址
        String host = "localhost";
        int port = 8080;       
        try {
        	
			List<String> serverPaths = client.getChildren().forPath(Constants.SERVER_PATH);
			//但是如果服务器死掉了，我们客户端也要知道，因此要加上监听
			CuratorWatcher watcher = new ServerWatcher();
			// 2. 让 ZooKeeper 客户端监听 "/netty" 节点下的子节点变化，并用上面的 watcher 处理事件
			// 作用：当服务端节点新增/删除时，ZooKeeper 会通知 watcher，触发后续处理
			client.getChildren().usingWatcher(watcher).forPath(Constants.SERVER_PATH);
			for(String serverPath: serverPaths) {
				String[] str = serverPath.split("#");
				//host
				//权重
				int weight = Integer.valueOf(str[2]);
				if(weight > 0) {
					//有多少权重，建立多少个链接，权重越高建立的连接越多，channel越多
					for(int w= 0;w<weight;w++) {
						ChannelManager.realServerPath.add(str[0]+"#"+str[1]);
						ChannelFuture channelFuture = TcpClient.b.connect(str[0], Integer.valueOf(str[1]));
						ChannelManager.addChannel(channelFuture);
					}
					//连接

				}
				
			}
			//如果目前zookeeper里面有可以连接的server
			if(ChannelManager.realServerPath.size()>0) {
				String[] hostAndPort = ChannelManager.realServerPath.toArray()[0].toString().split("#");
				host = hostAndPort[0];
				port = Integer.valueOf(hostAndPort[1]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

        
//        try {
//			f = b.connect(host, port).sync();
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} 
	}     

	//发送数据 长连接
	//每一个请求都是 同一个连接，因为用static修饰了
	//为每一个请求都用一个id来识别和修饰
	public static Response send(ClientRequest request) {
		
		//这里是主线程，如何通过异步的方式获得从线程得到server回复的response呢
		f = ChannelManager.get(ChannelManager.position);
		// 正确：合并 JSON 内容和分隔符，一次发送
		String msg = JSONObject.toJSONString(request) + "\r\n"; 
		f.channel().writeAndFlush(msg);
		//建立一个类，异步的通过锁的方式去获取从线程的数据
		
		DefaultFuture df = new DefaultFuture(request);
		return df.get(2*60*1000);
	}
}
