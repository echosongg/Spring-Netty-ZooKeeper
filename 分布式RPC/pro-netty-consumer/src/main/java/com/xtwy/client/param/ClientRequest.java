package com.xtwy.client.param;

import java.util.concurrent.atomic.AtomicLong;

public class ClientRequest {
	private long id;
	private Object content;
	//多个channel对应多个线程，因此request id是多个线程共享的资源，需要用线程安全的atomic来修饰
	private final AtomicLong aid = new AtomicLong(1);
	private String command;
	public ClientRequest() {
		id=aid.incrementAndGet();
	}
	public Object getContent() {
		return content;
	}
	public void setContent(Object content) {
		this.content = content;
	}
	public long getId() {
		return id;
	}
	public String getCommand() {
		return command;
	}
	public void setCommand(String command) {
		this.command = command;
	}
	
}
