package com.dxfx.netty.medium;

import java.lang.reflect.Method;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import com.dxfx.netty.annotation.Remote;
import com.dxfx.netty.annotation.RemoteInvoke;

@Component
//将数据交给user controller处理，用到反射
public class InitialMedium implements BeanPostProcessor{

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}
//初始化之后
	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		//获取所有remote类的名字和对应的方法，相当于对外界开了一个菜单，告诉他们哪些类的哪些方法是可以使用的，
		if(bean.getClass().isAnnotationPresent(Remote.class)) {
			System.out.println(bean.getClass().getName());
			Method[] methods = bean.getClass().getDeclaredMethods();
			//要有唯一识别id找到Controller以及他们对应的method
			for(Method m:methods) {
				//方法名和类名 放入map
				String key = bean.getClass().getInterfaces()[0].getName()+"."+m.getName();
				//media起到了一个电话本的作用，记录了controller和他们对应的method名字以及object,以及后续会对该请求进行操作
				Map<String,BeanMethod>beanMap = Media.beanMap;
				//得到一个对象同时存储bean and method对象
				BeanMethod beanMethod = new BeanMethod();
				beanMethod.setBean(bean);
				beanMethod.setMethod(m);
				beanMap.put(key, beanMethod);
			}
		}
		return bean;
	}
//中介要开始布局协调了，获取到所有的controller和它的方法

}
