package com.dxfx.netty.server;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;

import com.dxfx.netty.constant.Constants;
import com.dxfx.netty.factory.ZookeeperFactory;
import com.dxfx.netty.handler.simpleServerHandler;

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
public class NettyServer {
	//alt + / auto fulfill code
	public static void main(String[] args) {
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
						ch.pipeline().addLast(new simpleServerHandler());
						ch.pipeline().addLast(new StringEncoder());
					}
						
					});
		//ChannelFuture 调用 sync() 方法，会阻塞当前线程，直到端口绑定操作完成（成功或失败）
		int port = 8082;
		ChannelFuture f = bootstrap.bind(port).sync();
		//创建zookeeper curator客户端，将server连接到curator客户端上
		CuratorFramework client = ZookeeperFactory.create();
		//将服务器地址注册到zookeeper上
		InetAddress netAddress = InetAddress.getLocalHost();
		int weight = 1;
		//临时链接
		//client.create().withMode(CreateMode.EPHEMERAL).forPath(Constants.SERVER_PATH+ netAddress.getHostAddress());
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
}
