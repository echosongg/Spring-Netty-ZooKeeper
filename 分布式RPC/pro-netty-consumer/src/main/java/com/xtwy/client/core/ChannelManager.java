package com.xtwy.client.core;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import io.netty.channel.ChannelFuture;

//用来管理客户端的连接
public class ChannelManager {
	static AtomicInteger position = new AtomicInteger(0);
	static CopyOnWriteArrayList<String> realServerPath = new CopyOnWriteArrayList<String>();
	public static CopyOnWriteArrayList<ChannelFuture> channelFutures = new CopyOnWriteArrayList<>();
	
	public static void removeChannel(ChannelFuture channel) {
		channelFutures.remove(channel);
		
	}
	
	public static void addChannel(ChannelFuture channel) {
		channelFutures.add(channel);
		
	}
	public static void clear() {
		channelFutures.clear();
		
	}

	public static ChannelFuture get(AtomicInteger i) {
		int size = channelFutures.size();
		ChannelFuture channel = null;
		if(i.get()> size) {
			channel = channelFutures.get(0);
			ChannelManager.position = new AtomicInteger(1);
		}else {
			channel = channelFutures.get(i.getAndIncrement());
		}
		//如果链路不活跃，拿下一个链路并且移除掉这个链路
		if(!channel.channel().isActive()) {
			channelFutures.remove(channel);
			return get(position);
		}
		return channel;
	}
}
