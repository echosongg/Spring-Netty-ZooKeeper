package com.xtwy.client.core;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.xtwy.client.param.ClientRequest;
import com.xtwy.client.param.Response;


//在这个类里面，通过多线程处理，来获取来自从线程的数据
public class DefaultFuture {
	public final static ConcurrentHashMap<Long,DefaultFuture> allDefaultFuture =  new ConcurrentHashMap<Long, DefaultFuture>();
	
	//用于请求从线程的request数据的锁，condition这个锁对应的等待房间，一旦数据预备好主线程就去竞争从线程的数据的锁
	final Lock lock = new ReentrantLock();
	public Condition condition = lock.newCondition();
	private Response response;
	//两分钟没有收到回复就超时
	private long timeout=2*60*1000;
	public long getTimeout() {
		return timeout;
	}
	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}
	public long getStartTime() {
		return startTime;
	}
	private long startTime = System.currentTimeMillis();
	//构造函数
	public DefaultFuture(ClientRequest request) {
		allDefaultFuture.put(request.getId(), this);
	}
	//主线程获取数据，首先要等待从线程给它结果
	public Response get(long time) {
		//condition await signal
		lock.lock();
		//阻塞的获取response
		try {
			//如果已经有response消息了，唤醒线程去争夺锁，拿到数据
			System.out.println("尝试开始拿从线程的数据");
			while(!done()) {
			condition.await(time, TimeUnit.SECONDS);
			if((System.currentTimeMillis()-startTime)>time) {
				//
				System.out.println("请求超时");
				break;
			}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}finally {
			lock.unlock();
		}
		return this.response;
	}
	//设置response
	//被用在ClientSimpleHandler里面了，也就是从线程中
	public static void recieve(Response response) {
		DefaultFuture df = allDefaultFuture.get(response.getId());
		//这个时候已经有request了
		System.out.println("从线程开始");
		if(df != null) {
			Lock lock = df.lock;
			lock.lock();
			try {
				df.setResponse(response);
				//唤醒主线程来拿结果
				df.condition.signal();
				//主线程已经拿到response那么我们可以remove掉了
				allDefaultFuture.remove(df);
			} catch (Exception e) {
				e.printStackTrace();
			}finally {
				lock.unlock();
			}
			
		}
	}
	
	public Response getResponse() {
		return response;
	}
	public void setResponse(Response response) {
		this.response = response;
	}
	private boolean done() {
		if(this.response != null) {
			return true;
		}
		return false;
	}
	
	//开始一个线程，来remove掉超时的response
	static class FutureThread extends Thread{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			Set<Long> ids = allDefaultFuture.keySet();
			for(Long id: ids) {
				DefaultFuture df = allDefaultFuture.get(id);
				if(df == null) {
					allDefaultFuture.remove(df);
				}else {
					//加入已经超时了
					if(df.getTimeout()<System.currentTimeMillis()-df.getStartTime()) {
						Response resp = new Response();
						resp.setId(id);
						resp.setCode("22222");
						resp.setMsg("链路超时");
						recieve(resp);
					}
				}
			}
		}
		
	}
	static {
		FutureThread futureThread = new FutureThread();
		//后台辅助线程
		futureThread.setDaemon(true);
		futureThread.start();
	}
}
