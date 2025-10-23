package com.dxfx.netty.medium;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.springframework.cglib.beans.BeanMap;

import com.alibaba.fastjson.JSONObject;
import com.dxfx.netty.handler.param.ServerRequest;
import com.dxfx.netty.util.Response;

public class Media {
	public static Map<String, BeanMethod>beanMap;
	static {
		beanMap = new HashMap<String, BeanMethod>();
	}
	private static Media m = null;
	private Media() {
		
	}
	public static Media newInstance() {
		if (m == null) {
			m = new Media();
		}
		return m;
	}
	//关键！！！反射处理业务
	public Response process(ServerRequest request) {
		Response result = null;
		try {
			String command = request.getCommand();
			BeanMethod beanMethod = beanMap.get(command);
			if(beanMethod == null) {
				System.out.println("接口异常，没有对应的bean和方法");
				return null;
			}
			Object bean = beanMethod.getBean();
			Method m = beanMethod.getMethod();
			//得到方法的参数
			//m.getParameterTypes()[0] 返回 Class 类，核心原因是 Java 中所有类型（包括类、接口、基本类型等）的 “类型信息” 都由 java.lang.Class 类的实例来表示，
			//它是 Java 反射机制的基础
			Class<?> paramType = m.getParameterTypes()[0];
			//如果参数不是列表类型
			Object content = request.getContent();
			//将content从Json打包成m需要的参数类型，比如m是saveUser需要的参数类型就是User类
			@SuppressWarnings("unchecked")
			Object args = JSONObject.parseObject(JSONObject.toJSONString(content),paramType);
//			m.invoke() 是 Method 类的反射调用方法，第一个参数是要调用方法的对象实例（bean，这里指 UserController 的实例），第二个参数是方法的参数列表（args，即上面转换后的 User 对象）。
//			对于示例中的 saveUser 方法，这行代码等价于：userController.saveUser(user);（其中 user 是转换后的 User 对象）。
//			result 会接收方法的返回值（如果 saveUser 有返回值的话）。
			
			result = (Response) m.invoke(bean, args);
			result.setId(request.getId());
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
}
