package com.xtwy.client.zk;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.WatcherRemoveCuratorFramework;
import org.apache.curator.retry.ExponentialBackoffRetry;
import  org.apache.curator.RetryPolicy;
/*

Curator 客户端：扮演 “服务地址管理器” 的角色。
类似于一个单例模式
服务端启动时，通过它将自己的地址（如 127.0.0.1:8080）注册到 ZooKeeper 的节点（如 /rpc/services/UserService）。
客户端启动时，通过它从 ZooKeeper 读取 UserService 对应的服务地址列表，实现 “服务发现”。

*/
public class ZookeeperFactory {
	//用 static 修饰客户端实例，确保全局唯一
	public static CuratorFramework client;
	public static CuratorFramework create() {
		if(client == null) {
			//重试三次，隔一秒重连一次
			RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
			client = CuratorFrameworkFactory.newClient("localhost:2181", retryPolicy);
			client.start();
		}
		return client;
	}
	public static void main(String[] args) throws Exception {
		ZookeeperFactory z = new ZookeeperFactory();
		CuratorFramework client = create();
		//相当于在操作那个clicmd里面创建临时目录
		client.create().forPath("/netty");
	}
}
