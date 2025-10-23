package com.dxfx.server;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
//扫描所有子包的注解
@ComponentScan("com.dxfx")
public class SpringServer {
	public static void main(String[] args) throws InterruptedException {
		//相当于一个spring的初始化代码
		ApplicationContext context = new AnnotationConfigApplicationContext(SpringServer.class);
		//context.wait();
	}
}
