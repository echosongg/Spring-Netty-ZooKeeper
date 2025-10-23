package com.dxfx.netty.medium;

import java.lang.reflect.Method;

//bean and one of its method
public class BeanMethod {
	private Object Bean;
	private Method method;
	public Object getBean() {
		return Bean;
	}
	public void setBean(Object bean) {
		Bean = bean;
	}
	public Method getMethod() {
		return method;
	}
	public void setMethod(Method method) {
		this.method = method;
	}
	
}
