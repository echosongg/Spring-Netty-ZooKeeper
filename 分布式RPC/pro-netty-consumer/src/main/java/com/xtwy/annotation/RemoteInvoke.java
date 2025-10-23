package com.xtwy.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//基于方法
//ElementType.FIELD 限定注解只能用于类的成员变量（包括实例变量、静态变量、常量），是字段级注解的核心标记。
@Target({ElementType.FIELD}) 
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RemoteInvoke {

}
