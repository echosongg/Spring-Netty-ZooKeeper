package com.dxfx.netty.handler.param;
//和之前简单的不同，动态代理的request还需要知道要调用什么类，要调用什么方法
public class ServerRequest {
	private Long id;
	private Object content;
	private String command;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Object getContent() {
		return content;
	}
	public void setContent(Object content) {
		this.content = content;
	}
	public String getCommand() {
		return command;
	}
	public void setCommand(String command) {
		this.command = command;
	}
	
}
