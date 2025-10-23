package com.dxfx.netty.init;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.dxfx.netty.constant.Constants;
import com.dxfx.netty.factory.ZookeeperFactory;
import com.dxfx.netty.handler.ServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;

@Component
//implements ApplicationListener<ContextRefreshedEvent>：实现 Spring 的事件监听接口，
//监听 ContextRefreshedEvent 事件（该事件在 Spring 容器初始化完成后触发）。
//作用：当 Spring 容器加载完所有 Bean 后，自动执行 onApplicationEvent 方法，进而启动 Netty 服务（避免在容器未就绪时启动服务）。
public class NettyInitial implements ApplicationListener<ContextRefreshedEvent>{
	//alt + / auto fulfill code
	public void start() {
		EventLoopGroup parentGroup = new NioEventLoopGroup();
		//负责处理channel的read and write event
		EventLoopGroup childGroup = new NioEventLoopGroup();
		try {
					ServerBootstrap bootstrap = new ServerBootstrap();

		bootstrap.group(parentGroup, childGroup);
		//allow 128 channel in queue
		bootstrap.option(ChannelOption.SO_BACKLOG, 128)
		//设置心跳机制false 我们自己实现
				.childOption(ChannelOption.SO_KEEPALIVE, false)
				.channel(NioServerSocketChannel.class)
				.childHandler(new ChannelInitializer<Channel>() {
					@Override
					//每个channel对应一个channel pipeline，由channel所在的线程串行执行
					protected void initChannel(Channel ch) throws Exception {
						ch.pipeline().addLast(new DelimiterBasedFrameDecoder(6555, Delimiters.lineDelimiter()[0]));
						ch.pipeline().addLast(new StringDecoder());
						//设置自定义的心跳检测机制
						ch.pipeline().addLast(new IdleStateHandler(60, 45, 20, TimeUnit.SECONDS));
						ch.pipeline().addLast(new ServerHandler());
						ch.pipeline().addLast(new StringEncoder());
					}
						
					});
		//ChannelFuture 调用 sync() 方法，会阻塞当前线程，直到端口绑定操作完成（成功或失败）
		ChannelFuture f = bootstrap.bind(8080).sync();
		//创建zookeeper curator客户端，将server连接到curator客户端上
		CuratorFramework client = ZookeeperFactory.create();
		//将服务器地址注册到zookeeper上
		InetAddress netAddress = InetAddress.getLocalHost();
		
		//临时链接
		//加#是用来在client来获取server列表的时候，做分割
//		└── /server
//	    ├── /192.168.1.100#0000000001 （ServerA 注册的节点）
//	    └── /192.168.1.101#0000000002 （ServerB 注册的节点）
		int port = 8080;
		int weight = 2;
		client.create().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(Constants.SERVER_PATH+"/"+ netAddress.getHostAddress()+"#"+port+"#"+weight+"#");
		f.channel().closeFuture().sync();
		
		} catch (Exception e) {
			// TODO: handle exception
		    System.err.println("服务启动或 ZooKeeper 注册失败！原因：");
		    e.printStackTrace();  // 打印异常详情，例如是连接失败还是节点不存在

		}
		finally {
			parentGroup.shutdownGracefully();
			childGroup.shutdownGracefully();
		}
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		this.start();
		
	}
	
}
