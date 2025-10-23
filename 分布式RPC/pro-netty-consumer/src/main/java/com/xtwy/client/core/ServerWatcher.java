package com.xtwy.client.core;

import java.util.HashSet;
import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.zookeeper.WatchedEvent;

import com.xtwy.client.zk.ZookeeperFactory;

import io.netty.channel.ChannelFuture;

public class ServerWatcher implements CuratorWatcher{

	@Override
	public void process(WatchedEvent event) throws Exception {
		// 1. 重新获取 ZooKeeper 客户端（确保连接有效）
		CuratorFramework client = ZookeeperFactory.create();
		//发生事件的服务器path
		String path = event.getPath();
		// 3. 重新注册监听器（ZooKeeper 的 watch 是一次性的，需重新注册才能持续监听）
		client.getChildren().usingWatcher(this).forPath(path);
		// 4. 重新获取最新的服务端节点列表
		List<String> serverPaths = client.getChildren().forPath(path);
		// 5. 清空旧的服务地址集合，重新存入最新的服务地址（IP+端口）
		ChannelManager.realServerPath.clear(); // 先清空旧数据
//		来源：客户端从 serverPath 中解析、提取出的 “有用信息”，即 服务端的 IP 和端口。
//		例如，从 192.168.1.100#8080#0000000001 中拆分出 192.168.1.100#8080，存入 ChannelManager.realServerPath（HashSet 类型）。
		for(String serverPath: serverPaths) {
			String[] str = serverPath.split("#");
			int weight = Integer.valueOf(str[2]);
			if(weight > 0) {
				//有多少权重
				for(int w= 0;w<weight;w++) {
					ChannelManager.realServerPath.add(str[0]+"#"+str[1]);
					ChannelFuture channelFuture = TcpClient.b.connect(str[0], Integer.valueOf(str[1]));
					ChannelManager.addChannel(channelFuture);
				}
				//连接
			//host
			}
			ChannelManager.realServerPath.add(str[0]+"#"+str[1]);

		}
		ChannelManager.clear();// 先清空旧数据
		// 6. 清空旧的连接列表，重新建立与所有最新服务端的连接
		for(String realServer: ChannelManager.realServerPath) {
			String[] str = realServer.split("#");
			//host
			String host = str[0];
			int port = Integer.valueOf(str[1]);
			// 建立新连接并加入管理
				ChannelFuture channelFuture = TcpClient.b.connect(host, port);
				ChannelManager.addChannel(channelFuture);
		}

		}
	}

