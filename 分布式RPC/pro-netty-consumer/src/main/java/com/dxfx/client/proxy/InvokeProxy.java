package com.dxfx.client.proxy;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.stereotype.Component;

import com.dxfx.user.bean.User;
import com.xtwy.annotation.RemoteInvoke;
import com.xtwy.client.core.TcpClient;
import com.xtwy.client.param.ClientRequest;
import com.xtwy.client.param.Response;

@Component
public class InvokeProxy implements BeanPostProcessor{

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		//这个方法会在每个 Bean 初始化之前被调用，参数 bean 是当前正在初始化的 Bean 实例，
		//beanName 是 Bean 的名称。作用是：扫描当前 Bean 中带有 @RemoteInvoke 注解的字段，为其生成代理对象。
		Field[] fields = bean.getClass().getDeclaredFields();
		for(Field field : fields) {
			//判断是否上面有RemoteInvoke的注解，比如这里是UserRemote，他代表我们允许proxy帮助我们去向服务器发起对User操作的请求
			//因为remote方法是服务器和客户端商量好的，所以客户端一旦调用Useremote的方法，就会被系统识别到，并且打包成request发送给服务器，command由于是由类和类的方法组成，但是类和类的方法我们已经约定了，因此不需要我们手动的生成command，proxy会自动根据我们调用的函数去生成request
			if(field.isAnnotationPresent(RemoteInvoke.class)) {
				//设置字段可访问（即使是 private 也能修改其值）
				field.setAccessible(true);
				// 定义一个 Map，存储“方法与接口类型”的映射（后续生成调用命令用）
				final Map<Method, Class> methodClassMap = new HashMap<Method, Class>();
				putMethodClass(methodClassMap, field);
				//用 CGLIB 创建代理增强器（用于生成接口的代理对象）
				Enhancer enhancer = new Enhancer();
				//设置代理需要实现的接口（这里是 userRemote 的类型：UserRemote 接口）
				enhancer.setInterfaces(new Class[] {field.getType()});
				//设置代理的回调逻辑（当代理对象的方法被调用时，执行这里的代码）
				enhancer.setCallback(new MethodInterceptor() {
					//当调用被增强代理的方法，比如 userRemote.saveUser(u) 时，会进入这个方法
					@Override
					public Object intercept(Object instance, Method method, Object[] args, MethodProxy proxy)
							throws Throwable {
						//需要去netty调用服务器
						//生成调用命令：接口全类名 + 方法名（如 "com.dxfx.user.remote.UserRemote.saveUser"）
	                    // 从 map 中根据当前方法（saveUser）拿到接口类型（UserRemote.class），拼接成命令
						ClientRequest request = new ClientRequest();
						//需要动态的获取接口的名称和接口的方法
						request.setCommand(methodClassMap.get(method).getName()+"."+method.getName());
						request.setContent(args[0]);
						Response resp = TcpClient.send(request);
						return resp;
					}
					
				});

					try {
						// enhancer.create() 生成 UserRemote 接口的代理对象
		                // field.set(bean, ...) 把这个代理对象赋值给 RemoteInvokingTest 实例的 userRemote 字段
						field.set(bean, enhancer.create());
					} catch (IllegalArgumentException | IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

			}
		}
		return BeanPostProcessor.super.postProcessBeforeInitialization(bean, beanName);
	}
	//map 中会存入：UserRemote 的 saveUser 方法 → UserRemote.class，saveUsers 方法 → UserRemote.class
	private void putMethodClass(Map<Method, Class> methodClassMap, Field field) {
		Method[] methods = field.getType().getDeclaredMethods();
		for(Method m: methods) {
			methodClassMap.put(m, field.getType());
		}
		
	}
	//
	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		// TODO Auto-generated method stub
		return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
	}

}
