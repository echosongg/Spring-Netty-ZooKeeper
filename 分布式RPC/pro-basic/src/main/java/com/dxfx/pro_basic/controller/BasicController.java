
package com.dxfx.pro_basic.controller;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import com.dxfx.pro_basic.service.BasicService;
@Configuration
//扫描所有子包的注解
@ComponentScan("com.dxfx")
public class BasicController {
public static void main(String[] args) {
			ApplicationContext context = new AnnotationConfigApplicationContext(BasicController.class);
	        // 2. 从 Spring 容器中获取 BasicService 实例
	        // Spring 会自动实例化被 @Service 注解的 BasicService，并纳入容器管理，这里通过类型获取
			BasicService BasicService = context.getBean(BasicService.class);
			BasicService.testSaveUser();
}
}